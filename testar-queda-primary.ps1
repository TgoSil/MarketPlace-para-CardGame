<#
.SYNOPSIS
    Simula a queda do servidor PRIMARY e valida o failover automatico via Pgpool.

.DESCRIPTION
    Para cada servico (ou um especifico), este script executa:

    FASE 1 - PRE-QUEDA
      - Confirma que primary e replica estao saudaveis
      - Insere um dado de referencia e confirma replicacao

    FASE 2 - SIMULACAO DE QUEDA DO PRIMARY
      - Para o container do primary via docker stop
      - Aguarda o Pgpool detectar a falha e acionar o failover.sh

    FASE 3 - POS-FAILOVER (replica agora e primary)
      - Confirma que a replica foi promovida (pg_is_in_recovery = f)
      - Valida que escritas ainda funcionam (pela nova primary)
      - Valida que os dados pre-queda ainda estao acessiveis

    FASE 4 - RESTAURACAO (opcional)
      - Reinicia o container do antigo primary

.USAGE
    .\testar-queda-primary.ps1
    .\testar-queda-primary.ps1 -Servico catalog
    .\testar-queda-primary.ps1 -Servico inventory -TempoEspera 25
    .\testar-queda-primary.ps1 -PularRestauracao
#>

param(
    [string]$Servico          = "",
    [string]$PgUser           = "admin",
    [string]$PgPassword       = "1234",
    [int]   $TempoEspera      = 20,
    [switch]$PularRestauracao
)

# Mapeamento: prefixo -> banco de dados
$servicosDisponiveis = [ordered]@{
    "auth"      = "db"
    "inventory" = "inventory_db"
    "rewards"   = "rewards_db"
    "profile"   = "profile_db"
    "catalog"   = "catalog_db"
}

if ($Servico -ne "") {
    if (-not $servicosDisponiveis.Contains($Servico)) {
        Write-Host "Servico '$Servico' nao reconhecido. Opcoes: $($servicosDisponiveis.Keys -join ', ')" -ForegroundColor Red
        exit 1
    }
    $listaServicos = [ordered]@{ $Servico = $servicosDisponiveis[$Servico] }
} else {
    $listaServicos = $servicosDisponiveis
}

# Helpers
function Exec-Psql {
    param($container, $db, $sql)
    docker exec -e PGPASSWORD=$PgPassword $container `
        psql -U $PgUser -d $db -t -A -c $sql 2>&1
}

function Print-Header {
    param($msg, $cor = "Cyan")
    Write-Host ""
    Write-Host ("=" * 62) -ForegroundColor $cor
    Write-Host "  $msg" -ForegroundColor $cor
    Write-Host ("=" * 62) -ForegroundColor $cor
}

function Print-Ok   { param($msg) Write-Host "  [OK]     $msg" -ForegroundColor Green      }
function Print-Fail { param($msg) Write-Host "  [FALHOU] $msg" -ForegroundColor Red        }
function Print-Info { param($msg) Write-Host "  [INFO]   $msg" -ForegroundColor Yellow     }
function Print-Warn { param($msg) Write-Host "  [AVISO]  $msg" -ForegroundColor DarkYellow }
function Print-Step { param($msg) Write-Host "`n  >>> $msg"    -ForegroundColor Magenta    }

function Container-Status {
    param($name)
    return (docker inspect $name --format='{{.State.Status}}' 2>$null)
}

function Wait-Promotion {
    param($replicaContainer, $db, $maxSegundos = 90)
    Write-Host ""
    Print-Info "Aguardando Pgpool acionar failover e a replica ser promovida... (max $maxSegundos s)"
    $inicio = Get-Date
    while ($true) {
        $resultado = Exec-Psql -container $replicaContainer -db $db -sql "SELECT pg_is_in_recovery();"
        if ($resultado -match "^f$") {
            return $true
        }
        $decorrido = ((Get-Date) - $inicio).TotalSeconds
        if ($decorrido -ge $maxSegundos) {
            return $false
        }
        Write-Host "    ...replica ainda em standby ($([math]::Round($decorrido))s)" -ForegroundColor DarkGray
        Start-Sleep -Seconds 5
    }
}

# Tabela de resultados
$resultados = @()

# =============================================
# LOOP PRINCIPAL POR SERVICO
# =============================================
foreach ($prefixo in $listaServicos.Keys) {

    $db        = $listaServicos[$prefixo]
    $contA     = "$prefixo-service-db-primary"
    $contB     = "$prefixo-service-db-replica"

    $r = [ordered]@{
        Servico             = $prefixo
        PreQueda_OK         = "-"
        FailoverOK          = "-"
        EscritaNovaOK       = "-"
        DadosDisponiveis_OK = "-"
    }

    Print-Header "SERVICO: $($prefixo.ToUpper())" "Cyan"

    # -----------------------------------------
    # FASE 1: PRE-QUEDA
    # -----------------------------------------
    Print-Step "FASE 1: Verificacao pre-queda"

    $statusContA = Container-Status $contA
    $statusContB = Container-Status $contB

    if ($statusContA -ne "running" -or $statusContB -ne "running") {
        Print-Fail "Containers nao estao rodando. Pulando servico."
        $r.PreQueda_OK = "FALHOU"
        $resultados += [pscustomobject]$r
        continue
    }

    $recA = Exec-Psql -container $contA -db $db -sql "SELECT pg_is_in_recovery();"
    $recB = Exec-Psql -container $contB -db $db -sql "SELECT pg_is_in_recovery();"
    
    $primary = ""
    $replica = ""

    if (($recA -match "^f$") -and ($recB -match "^t$")) {
        $primary = $contA
        $replica = $contB
        Print-Info "Roles: Primary = $primary | Replica = $replica"
    } elseif (($recA -match "^t$") -and ($recB -match "^f$")) {
        $primary = $contB
        $replica = $contA
        Print-Info "Roles invertidos: Primary = $primary | Replica = $replica"
    } else {
        Print-Fail "Nao foi possivel determinar os papeis. $contA=$recA, $contB=$recB"
        $r.PreQueda_OK = "FALHOU"
        $resultados += [pscustomobject]$r
        continue
    }

    Print-Ok "Primary e replica em execucao e identificados"

    $marcadorPreQueda = "pre_queda_primary_$(Get-Random)"
    Exec-Psql -container $primary -db $db -sql "CREATE TABLE IF NOT EXISTS teste_failover_primary(id serial, fase text, msg text);" | Out-Null

    $insertResult = Exec-Psql -container $primary -db $db -sql "INSERT INTO teste_failover_primary(fase, msg) VALUES ('pre-queda','$marcadorPreQueda');"
    if ($insertResult -match "INSERT") {
        Print-Ok "Dado inserido no primary pre-queda"
    } else {
        Print-Fail "Falha ao inserir no primary: $insertResult"
        $r.PreQueda_OK = "FALHOU"
        $resultados += [pscustomobject]$r
        continue
    }

    Start-Sleep -Seconds 2

    $achouPreQueda = Exec-Psql -container $replica -db $db -sql "SELECT count(*) FROM teste_failover_primary WHERE msg='$marcadorPreQueda';"
    if ($achouPreQueda -match "^\d+$" -and [int]$achouPreQueda -ge 1) {
        Print-Ok "Dado pre-queda replicou para a replica corretamente"
        $r.PreQueda_OK = "PASS"
    } else {
        Print-Fail "Dado pre-queda NAO replicou (resposta: $achouPreQueda)"
        $r.PreQueda_OK = "FALHOU"
        $resultados += [pscustomobject]$r
        continue
    }

    # -----------------------------------------
    # FASE 2: SIMULACAO DE QUEDA DO PRIMARY
    # -----------------------------------------
    Print-Step "FASE 2: Derrubando o PRIMARY ($primary)..."
    docker stop $primary | Out-Null
    Print-Info "Primary parado. Aguardando $TempoEspera s para o Pgpool detectar e chamar failover.sh..."
    Start-Sleep -Seconds $TempoEspera

    if ((Container-Status $primary) -ne "running") {
        Print-Ok "Container do primary confirmado como parado"
    } else {
        Print-Warn "Container do primary ainda parece ativo. Verifique."
    }

    # -----------------------------------------
    # FASE 3: AGUARDA PROMOCAO DA REPLICA
    # -----------------------------------------
    Print-Step "FASE 3: Verificando se o failover automatico ocorreu..."

    $foiPromovida = Wait-Promotion -replicaContainer $replica -db $db -maxSegundos 90

    if ($foiPromovida) {
        Print-Ok "Replica promovida a PRIMARY com sucesso! (pg_is_in_recovery = f)"
        $r.FailoverOK = "PASS"
    } else {
        Print-Fail "Replica NAO foi promovida dentro de 90s. Failover nao funcionou."
        $r.FailoverOK = "FALHOU"
        if (-not $PularRestauracao) {
            docker start $primary | Out-Null
            Print-Info "Primary reiniciado para restaurar o ambiente"
        }
        Exec-Psql -container $replica -db $db -sql "DROP TABLE IF EXISTS teste_failover_primary;" | Out-Null
        $resultados += [pscustomobject]$r
        continue
    }

    # Valida escrita na nova primary (antiga replica)
    $marcadorPosFailover = "pos_failover_$(Get-Random)"
    $escritaPosFailover = Exec-Psql -container $replica -db $db `
        -sql "INSERT INTO teste_failover_primary(fase, msg) VALUES ('pos-failover','$marcadorPosFailover');"

    if ($escritaPosFailover -match "INSERT") {
        Print-Ok "Escrita na nova primary (antiga replica) funcionou!"
        $r.EscritaNovaOK = "PASS"
    } else {
        Print-Fail "Falha ao escrever na nova primary: $escritaPosFailover"
        $r.EscritaNovaOK = "FALHOU"
    }

    # Dados pre-queda ainda acessiveis?
    $dadosPreQueda = Exec-Psql -container $replica -db $db `
        -sql "SELECT count(*) FROM teste_failover_primary WHERE msg='$marcadorPreQueda';"

    if ($dadosPreQueda -match "^\d+$" -and [int]$dadosPreQueda -ge 1) {
        Print-Ok "Dados pre-queda ainda acessiveis na nova primary"
        $r.DadosDisponiveis_OK = "PASS"
    } else {
        Print-Fail "Dados pre-queda NAO encontrados: $dadosPreQueda"
        $r.DadosDisponiveis_OK = "FALHOU"
    }

    # -----------------------------------------
    # FASE 4: RESTAURACAO DO AMBIENTE
    # -----------------------------------------
    if ($PularRestauracao) {
        Print-Info "Flag -PularRestauracao ativa. Antigo primary permanece desligado."
        Print-Warn "A replica (agora primary) continuara aceitando leituras e escritas."
        Print-Warn "Para reintegrar o antigo primary como replica, reinicie o container e reconfigure."
    } else {
        Print-Step "FASE 4: Reiniciando antigo primary ($primary) para restaurar ambiente de testes..."
        docker start $primary | Out-Null
        Start-Sleep -Seconds 5

        $recoveryAntigoPrimary = Exec-Psql -container $primary -db $db -sql "SELECT pg_is_in_recovery();" 2>$null
        if ($recoveryAntigoPrimary -match "^t$") {
            Print-Ok "Antigo primary reiniciou como REPLICA (standby). Ambiente normalizado."
        } else {
            Print-Warn "Antigo primary reiniciou mas NAO esta em modo standby."
            Print-Warn "Pode haver split-brain. Avalie manualmente o estado dos containers."
            Print-Warn "Para reintegrar: reconfigure o antigo primary como replica do novo primary."
        }
    }

    # Limpa tabela de teste
    Exec-Psql -container $replica -db $db -sql "DROP TABLE IF EXISTS teste_failover_primary;" | Out-Null
    Print-Info "Tabela de teste removida"

    $resultados += [pscustomobject]$r
    Write-Host ""
}

# Resumo final
Print-Header "RESUMO FINAL" "Cyan"
$resultados | Format-Table -AutoSize

$falhas = $resultados | Where-Object {
    $_.PreQueda_OK         -eq "FALHOU" -or
    $_.FailoverOK          -eq "FALHOU" -or
    $_.EscritaNovaOK       -eq "FALHOU" -or
    $_.DadosDisponiveis_OK -eq "FALHOU"
}

Write-Host ""
if ($falhas.Count -eq 0) {
    Write-Host "Todos os servicos passaram no teste de queda do PRIMARY e failover automatico." -ForegroundColor Green
} else {
    Write-Host "$($falhas.Count) servico(s) com falha: $($falhas.Servico -join ', ')" -ForegroundColor Red
}

Write-Host ""
Write-Host "IMPORTANTE: Apos este teste, valide o estado dos containers com:" -ForegroundColor Yellow
Write-Host "  docker compose ps" -ForegroundColor Yellow
Write-Host "  docker compose up -d   (para restaurar containers parados)" -ForegroundColor Yellow
Write-Host ""
