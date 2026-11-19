#!/usr/bin/env bash
set -Eeuo pipefail
cd /opt/bairro
printf '\n=== Data UTC ===\n'
date -u --iso-8601=seconds
printf '\n=== Commit da aplicação ===\n'
git rev-parse HEAD
printf '\n=== Docker e Compose ===\n'
docker --version
docker compose version
printf '\n=== Containers em execução ===\n'
docker compose ps
printf '\n=== Usuário e diretório da aplicação ===\n'
docker compose exec -T api id
docker compose exec -T api pwd
printf '\n=== Estrutura do projeto ===\n'
find . -maxdepth 2 -type f ! -path './.git/*' ! -name '.env' | sort
printf '\n=== Rede e volume ===\n'
docker inspect "$(docker compose ps -q api)" --format '{{json .NetworkSettings.Networks}}'
docker inspect "$(docker compose ps -q banco)" --format '{{json .Mounts}}'
printf '\n=== Logs ===\n'
docker compose logs --tail=25 api banco
printf '\n=== SELECT direto no PostgreSQL ===\n'
docker compose exec -T banco psql -U bairro -d bairro -c \
    'SELECT s.id, s.titulo, s.status, c.nome AS categoria FROM solicitacoes s JOIN categorias c ON c.id = s.categoria_id ORDER BY s.id;'
printf '\n=== Resposta OpenAPI ===\n'
curl -fsS http://127.0.0.1:8080/v3/api-docs -o /tmp/bairro-openapi.json
python3 -c 'import json; d=json.load(open("/tmp/bairro-openapi.json")); print(d["info"]); print("Rotas:", len(d["paths"]))'
