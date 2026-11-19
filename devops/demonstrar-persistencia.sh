#!/usr/bin/env bash
set -Eeuo pipefail
cd /opt/bairro
printf '=== Containers antes da recriação ===\n'
docker compose ps -q
python3 devops/demonstrar-crud.py --verificar-persistencia
docker compose down
docker compose up -d
for tentativa in $(seq 1 90); do
    if curl -fsS http://127.0.0.1:8080/v3/api-docs -o /dev/null 2>/dev/null; then
        printf '\n=== Containers depois da recriação ===\n'
        docker compose ps -q
        python3 devops/demonstrar-crud.py --verificar-persistencia
        printf '\n=== SELECT direto após recriação ===\n'
        docker compose exec -T banco psql -U bairro -d bairro -c \
            "SELECT id, titulo, status FROM solicitacoes WHERE id = $(cat persistencia-id.txt);"
        exit 0
    fi
    sleep 5
done
printf 'A API não ficou pronta no prazo esperado.\n' >&2
exit 1
