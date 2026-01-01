# Bootstrap script para a API Search Credit (Windows PowerShell)
# Cria a rede Docker compartilhada e sobe a infraestrutura base da API

Write-Host "=== Bootstrap da API Search Credit ===" -ForegroundColor Cyan
Write-Host ""

# Verificar se a rede Docker existe
Write-Host "Verificando rede Docker 'search-credit-network'..." -ForegroundColor Yellow
$networkExists = docker network inspect search-credit-network 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Rede 'search-credit-network' já existe" -ForegroundColor Green
} else {
    Write-Host "Criando rede 'search-credit-network'..." -ForegroundColor Yellow
    docker network create search-credit-network
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Rede 'search-credit-network' criada com sucesso" -ForegroundColor Green
    } else {
        Write-Host "✗ Erro ao criar rede Docker" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "Subindo containers da API..." -ForegroundColor Yellow
docker compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "=== Bootstrap concluído ===" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Containers em execução:" -ForegroundColor Yellow
    docker compose ps
} else {
    Write-Host "✗ Erro ao subir containers" -ForegroundColor Red
    exit 1
}

