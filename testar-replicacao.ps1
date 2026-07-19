<#
.SYNOPSIS
    Testa a replicacao PostgreSQL (primary -> replica) de todos os servicos do projeto
    e gera um relatorio PASS/FALHOU no terminal.

.USAGE
    .\testar-replicacao.ps1
    .\testar-replicacao.ps1 -Servico auth        (testa so um servico)
    .\testar-replicacao.ps1 -PgPassword 1234     (senha customizada)
#>

param(
    [string]$Servico = "",
    [string]$PgUser = "admin",
    [string]$PgPassword = "1234"
)

# Mapeamento prefixo -> nome do banco de dados
$servicos = @{
    "auth"      = "db"
    "inventory" = "inventory_db"
    "rewards"   = "rewards_db"
    "profile"   = "profile_db"
    "catalog"   = "catalog_db"
}

if ($Servico -ne "") {
    if (-not $servicos.ContainsKey($Servico)) {
        Write-Host "Servico '$Servico' nao reconhecido. Opcoes: $($servicos.Keys -join ', ')" -ForegroundColor Red
        exit 1
    }
    $listaServicos = @{ $Servico = $servicos[$Servico] }
} else {
    $listaServicos = $servicos
}

$resultados = @()

function Run-PsqlPrimary {
    param($container, $db, $sql)
    docker exec -e PGPASSWORD=$PgPassword $container psql -U $PgUser -d $db -t -A -c $sql 2>&1
}

Write-Host ""
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "  TESTE DE REPLICACAO POSTGRESQL - MarketShare" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host ""

foreach ($prefixo in $listaServicos.Keys) {

    $db          = $listaServicos[$prefixo]
    $primary     = "$prefixo-service-db-primary"
    $replica     = "$prefixo-service-db-replica"

    Write-Host "----- [$prefixo] -----" -ForegroundColor Yellow

    $status = [ordered]@{
        Servico            = $prefixo
        ContainersUp       = "?"
        ReplicaConectado   = "?"
        ReplicaEmStandby   = "?"
        DadosReplicaram    = "?"
    }

    # 1) Containers estao "Up"?
    $psPrimary = docker inspect $primary --format='{{.State.Status}}' 2>$null
    $psReplica = docker inspect $replica --format='{{.State.Status}}' 2>$null

    if ($psPrimary -eq "running" -and $psReplica -eq "running") {
        $status.ContainersUp = "PASS"
        Write-Host "  [OK] primary e replica estao rodando" -ForegroundColor Green
    } else {
        $status.ContainersUp = "FALHOU"
        Write-Host "  [FALHOU] primary=$psPrimary replica=$psReplica" -ForegroundColor Red
        $resultados += [pscustomobject]$status
        Write-Host ""
        continue
    }

    # 2) Primary enxerga o replica conectado?
    $repl = Run-PsqlPrimary -container $primary -db $db -sql "SELECT count(*) FROM pg_stat_replication;"
    if ($repl -match "^\d+$" -and [int]$repl -ge 1) {
        $status.ReplicaConectado = "PASS"
        Write-Host "  [OK] primary detecta $repl replica(s) conectado(s)" -ForegroundColor Green
    } else {
        $status.ReplicaConectado = "FALHOU"
        Write-Host "  [FALHOU] pg_stat_replication vazio ou erro: $repl" -ForegroundColor Red
    }

    # 3) Replica esta em modo standby?
    $recovery = Run-PsqlPrimary -container $replica -db $db -sql "SELECT pg_is_in_recovery();"
    if ($recovery -match "^t$") {
        $status.ReplicaEmStandby = "PASS"
        Write-Host "  [OK] replica esta em modo standby (pg_is_in_recovery = t)" -ForegroundColor Green
    } else {
        $status.ReplicaEmStandby = "FALHOU"
        Write-Host "  [FALHOU] resposta inesperada: $recovery" -ForegroundColor Red
    }

    # 4) Teste real de propagacao de dados
    $marcador = "teste_$(Get-Random)"
    Run-PsqlPrimary -container $primary -db $db -sql "CREATE TABLE IF NOT EXISTS teste_replicacao(id serial, msg text);" | Out-Null
    Run-PsqlPrimary -container $primary -db $db -sql "INSERT INTO teste_replicacao(msg) VALUES ('$marcador');" | Out-Null

    Start-Sleep -Seconds 2

    $achou = Run-PsqlPrimary -container $replica -db $db -sql "SELECT count(*) FROM teste_replicacao WHERE msg='$marcador';"
    if ($achou -match "^\d+$" -and [int]$achou -ge 1) {
        $status.DadosReplicaram = "PASS"
        Write-Host "  [OK] dado inserido no primary apareceu no replica" -ForegroundColor Green
    } else {
        $status.DadosReplicaram = "FALHOU"
        Write-Host "  [FALHOU] dado nao propagou (resposta: $achou)" -ForegroundColor Red
    }

    $resultados += [pscustomobject]$status
    Write-Host ""
}

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "  RESUMO FINAL" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan
$resultados | Format-Table -AutoSize

$falhas = $resultados | Where-Object {
    $_.ContainersUp -eq "FALHOU" -or
    $_.ReplicaConectado -eq "FALHOU" -or
    $_.ReplicaEmStandby -eq "FALHOU" -or
    $_.DadosReplicaram -eq "FALHOU"
}

if ($falhas.Count -eq 0) {
    Write-Host "Todos os servicos passaram nos testes de replicacao." -ForegroundColor Green
} else {
    Write-Host "$($falhas.Count) servico(s) com falha: $($falhas.Servico -join ', ')" -ForegroundColor Red
}
