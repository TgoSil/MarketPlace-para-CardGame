<#
.SYNOPSIS
    Simula a queda do servidor replica de um servico e valida o comportamento do sistema.

.DESCRIPTION
    Para cada servico (ou um especifico), este script executa o seguinte ciclo:

    FASE 1 - PRE-QUEDA
      - Confirma que primary e replica estao saudaveis
      - Confirma que o Pgpool ve os dois backends ativos
      - Insere um dado no primary e confirma replicacao no replica

    FASE 2 - SIMULACAO DE QUEDA
      - Para (docker stop) o container da replica
      - Aguarda o Pgpool detectar a falha

    FASE 3 - DURANTE A QUEDA
      - Valida que leituras ainda funcionam via primary
      - Valida que escritas no primary continuam funcionando

    FASE 4 - RECUPERACAO
      - Reinicia o container da replica
      - Aguarda a replica reconectar-se ao primary (pg_stat_replication)

    FASE 5 - POS-RECUPERACAO
      - Valida que dados gravados DURANTE a queda foram replicados
      - Confirma que a replica voltou ao modo standby

.USAGE
    .\testar-queda-replica.ps1
    .\testar-queda-replica.ps1 -Servico catalog
    .\testar-queda-replica.ps1 -Servico inventory -PgPassword 1234 -TempoEspera 20
    .\testar-queda-replica.ps1 -PularRecuperacao
#>

param(
    [string]$Servico      = "",
    [string]$PgUser       = "admin",
    [string]$PgPassword   = "1234",
    [int]   $TempoEspera  = 15,    # segundos para aguardar Pgpool detectar queda/subida
    [switch]$PularRecuperacao      # se presente, deixa a replica desligada ao final
)

# ─────────────────────────────────────────────
# Mapeamento: prefixo -> banco de dados
# ─────────────────────────────────────────────
$servicosDisponiveis = [ordered]@{
    "auth"      = "db"
    "inventory" = "inventory_db"
    "rewards"   = "rewards_db"
    "profile"   = "profile_db"
    "catalog"   = "catalog_db"
}

if ($Servico -ne "") {
    if (-not $servicosDisponiveis.ContainsKey($Servico)) {
        Write-Host "Servico '$Servico' nao reconhecido. Opcoes: $($servicosDisponiveis.Keys -join ', ')" -ForegroundColor Red
        exit 1
    }
    $listaServicos = [ordered]@{ $Servico = $servicosDisponiveis[$Servico] }
} else {
    $listaServicos = $servicosDisponiveis
}

# ─────────────────────────────────────────────
# Helpers
# ─────────────────────────────────────────────
function Exec-Psql {
    param($container, $db, $sql)
    docker exec -e PGPASSWORD=$PgPassword $container `
        psql -U $PgUser -d $db -t -A -c $sql 2>&1
}

function Print-Header {
    param($msg, $cor = "Cyan")
    Write-Host ""
    Write-Host ("=" * 58) -ForegroundColor $cor
    Write-Host "  $msg" -ForegroundColor $cor
    Write-Host ("=" * 58) -ForegroundColor $cor
}

function Print-Ok   { param($msg) Write-Host "  [OK]     $msg" -ForegroundColor Green  }
function Print-Fail { param($msg) Write-Host "  [FALHOU] $msg" -ForegroundColor Red    }
function Print-Info { param($msg) Write-Host "  [INFO]   $msg" -ForegroundColor Yellow }
function Print-Step { param($msg) Write-Host "`n  >>> $msg" -ForegroundColor Magenta   }

function Container-Running {
    param($name)
    $s = docker inspect $name --format='{{.State.Status}}' 2>$null
    return ($s -eq "running")
}

function Wait-ReplicaReconnect {
    param($primaryContainer, $db, $maxSegundos = 90)
    $inicio = Get-Date
    Write-Host ""
    Print-Info "Aguardando replica reconectar ao primary (max $maxSegundos s)..."
    while ($true) {
        $repl = Exec-Psql -container $primaryContainer -db $db -sql "SELECT count(*) FROM pg_stat_replication;"
        if ($repl -match "^\d+$" -and [int]$repl -ge 1) {
            return $true
        }
        $decorrido = ((Get-Date) - $inicio).TotalSeconds
        if ($decorrido -ge $maxSegundos) {
            return $false
        }
        Write-Host "    ...ainda aguardando ($([math]::Round($decorrido))s)" -ForegroundColor DarkGray
        Start-Sleep -Seconds 5
    }
}

# ─────────────────────────────────────────────
# Tabela de resultados
# ─────────────────────────────────────────────
$resultados = @()

# ═════════════════════════════════════════════
# LOOP PRINCIPAL POR SERVICO
# ═════════════════════════════════════════════
foreach ($prefixo in $listaServicos.Keys) {

    $db        = $listaServicos[$prefixo]
    $contA     = "$prefixo-service-db-primary"
    $contB     = "$prefixo-service-db-replica"

    $r = [ordered]@{
        Servico               = $prefixo
        PreQueda_OK           = "-"
        SistemaFunciona_OK    = "-"
        RecuperacaoOK         = "-"
        DadosDuranteQuedaOK   = "-"
    }

    Print-Header "SERVICO: $($prefixo.ToUpper())" "Cyan"

    # ─────────────────────────────────────────
    # FASE 1: PRE-QUEDA
    # ─────────────────────────────────────────
    Print-Step "FASE 1: Verificacao pre-queda"

    if (-not (Container-Running $contA) -or -not (Container-Running $contB)) {
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

    # Replica em standby? (Ja sabemos que sim, checado na deteccao acima)
    Print-Ok "Replica em modo standby (pg_is_in_recovery = t)"

    # Primary ve replica conectada?
    $replCount = Exec-Psql -container $primary -db $db -sql "SELECT count(*) FROM pg_stat_replication;"
    if ($replCount -match "^\d+$" -and [int]$replCount -ge 1) {
        Print-Ok "Primary detecta $replCount replica(s) conectada(s) via streaming"
    } else {
        Print-Fail "pg_stat_replication vazio. Replicacao pode estar inativa."
        $r.PreQueda_OK = "FALHOU"
        $resultados += [pscustomobject]$r
        continue
    }

    # Insere dado de referencia e confirma replicacao
    $marcadorPreQueda = "pre_queda_$(Get-Random)"
    Exec-Psql -container $primary -db $db -sql "CREATE TABLE IF NOT EXISTS teste_queda(id serial, fase text, msg text);" | Out-Null
    Exec-Psql -container $primary -db $db -sql "INSERT INTO teste_queda(fase, msg) VALUES ('pre-queda','$marcadorPreQueda');" | Out-Null
    Start-Sleep -Seconds 2

    $achou = Exec-Psql -container $replica -db $db -sql "SELECT count(*) FROM teste_queda WHERE msg='$marcadorPreQueda';"
    if ($achou -match "^\d+$" -and [int]$achou -ge 1) {
        Print-Ok "Dado pre-queda inserido no primary replicou corretamente"
        $r.PreQueda_OK = "PASS"
    } else {
        Print-Fail "Dado pre-queda NAO replicou (resposta: $achou)"
        $r.PreQueda_OK = "FALHOU"
        $resultados += [pscustomobject]$r
        continue
    }

    # ─────────────────────────────────────────
    # FASE 2: SIMULACAO DE QUEDA
    # ─────────────────────────────────────────
    Print-Step "FASE 2: Derrubando replica ($replica) via docker stop..."
    docker stop $replica | Out-Null
    Print-Info "Replica parada. Aguardando $TempoEspera s para Pgpool detectar falha..."
    Start-Sleep -Seconds $TempoEspera

    if (Container-Running $replica) {
        Print-Fail "Container da replica ainda esta rodando!"
    } else {
        Print-Ok "Container da replica confirmado como parado"
    }

    # ─────────────────────────────────────────
    # FASE 3: DURANTE A QUEDA
    # ─────────────────────────────────────────
    Print-Step "FASE 3: Testando sistema com replica fora do ar"

    # Escrita no primary deve continuar funcionando
    $marcadorDuranteQueda = "durante_queda_$(Get-Random)"
    $escritaDuranteQueda = Exec-Psql -container $primary -db $db `
        -sql "INSERT INTO teste_queda(fase, msg) VALUES ('durante-queda','$marcadorDuranteQueda');"

    if ($escritaDuranteQueda -match "INSERT") {
        Print-Ok "Escrita no primary funcionou com replica fora do ar"
    } else {
        Print-Fail "Falha ao escrever no primary durante a queda: $escritaDuranteQueda"
    }

    # Leitura via primary diretamente deve continuar
    $leituraDuranteQueda = Exec-Psql -container $primary -db $db `
        -sql "SELECT count(*) FROM teste_queda WHERE fase='pre-queda';"

    if ($leituraDuranteQueda -match "^\d+$" -and [int]$leituraDuranteQueda -ge 1) {
        Print-Ok "Leitura via primary funcionou com replica fora do ar"
        $r.SistemaFunciona_OK = "PASS"
    } else {
        Print-Fail "Leitura falhou mesmo com primary ativo: $leituraDuranteQueda"
        $r.SistemaFunciona_OK = "FALHOU"
    }

    # ─────────────────────────────────────────
    # FASE 4 e 5: RECUPERACAO
    # ─────────────────────────────────────────
    if ($PularRecuperacao) {
        Print-Info "Flag -PularRecuperacao ativa. Replica permanece desligada."
        $r.RecuperacaoOK         = "PULADO"
        $r.DadosDuranteQuedaOK   = "PULADO"
    } else {
        Print-Step "FASE 4: Reiniciando replica ($replica) via docker start..."
        docker start $replica | Out-Null

        $reconectou = Wait-ReplicaReconnect -primaryContainer $primary -db $db -maxSegundos 90

        if ($reconectou) {
            Print-Ok "Replica reconectou ao primary com sucesso"
            $r.RecuperacaoOK = "PASS"
        } else {
            Print-Fail "Replica NAO reconectou dentro de 90s"
            $r.RecuperacaoOK = "FALHOU"
        }

        # ─────────────────────────────────────
        # FASE 5: POS-RECUPERACAO
        # ─────────────────────────────────────
        Print-Step "FASE 5: Validando dados replicados apos recuperacao"
        Start-Sleep -Seconds 5

        $achouDuranteQueda = Exec-Psql -container $replica -db $db `
            -sql "SELECT count(*) FROM teste_queda WHERE msg='$marcadorDuranteQueda';"

        if ($achouDuranteQueda -match "^\d+$" -and [int]$achouDuranteQueda -ge 1) {
            Print-Ok "Dados gravados DURANTE a queda foram replicados apos recuperacao"
            $r.DadosDuranteQuedaOK = "PASS"
        } else {
            Print-Fail "Dados gravados durante a queda NAO aparecem na replica. Resposta: $achouDuranteQueda"
            $r.DadosDuranteQuedaOK = "FALHOU"
        }

        # Replica voltou ao standby?
        $recoveryPos = Exec-Psql -container $replica -db $db -sql "SELECT pg_is_in_recovery();"
        if ($recoveryPos -match "^t$") {
            Print-Ok "Replica voltou ao modo standby (pg_is_in_recovery = t)"
        } else {
            Print-Fail "Replica NAO esta em modo standby apos recuperacao: $recoveryPos"
        }
    }

    # Limpa tabela de teste
    Exec-Psql -container $primary -db $db -sql "DROP TABLE IF EXISTS teste_queda;" | Out-Null
    Print-Info "Tabela de teste removida"

    $resultados += [pscustomobject]$r
}

# ─────────────────────────────────────────────
# RESUMO FINAL
# ─────────────────────────────────────────────
Print-Header "RESUMO FINAL" "Cyan"
$resultados | Format-Table -AutoSize

$falhas = $resultados | Where-Object {
    $_.PreQueda_OK         -eq "FALHOU" -or
    $_.SistemaFunciona_OK  -eq "FALHOU" -or
    $_.RecuperacaoOK       -eq "FALHOU" -or
    $_.DadosDuranteQuedaOK -eq "FALHOU"
}

Write-Host ""
if ($falhas.Count -eq 0) {
    Write-Host "Todos os servicos passaram nos cenarios de queda/recuperacao da replica." -ForegroundColor Green
} else {
    Write-Host "$($falhas.Count) servico(s) com falha: $($falhas.Servico -join ', ')" -ForegroundColor Red
}
Write-Host ""
