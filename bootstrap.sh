#!/bin/bash

# Bootstrap script para a API Search Credit
# Cria a rede Docker compartilhada e sobe a infraestrutura base da API

set -e

echo "=== Bootstrap da API Search Credit ==="
echo ""

# Verificar se a rede Docker existe
echo "Verificando rede Docker 'search-credit-network'..."
if docker network inspect search-credit-network > /dev/null 2>&1; then
    echo "✓ Rede 'search-credit-network' já existe"
else
    echo "Criando rede 'search-credit-network'..."
    docker network create search-credit-network
    echo "✓ Rede 'search-credit-network' criada com sucesso"
fi

echo ""
echo "Subindo containers da API..."
docker compose up -d --build

echo ""
echo "=== Bootstrap concluído ==="
echo ""
echo "Containers em execução:"
docker compose ps

