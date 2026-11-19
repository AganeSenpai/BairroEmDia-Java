#!/usr/bin/env bash
set -Eeuo pipefail
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y ca-certificates curl git openssl python3
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
. /etc/os-release
cat > /etc/apt/sources.list.d/docker.sources <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: ${UBUNTU_CODENAME:-$VERSION_CODENAME}
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable --now docker
install -d -m 0755 /opt/bairro
if [[ ! -d /opt/bairro/.git ]]; then
    git clone https://github.com/AganeSenpai/BairroEmDia-Java.git /opt/bairro
fi
cd /opt/bairro
exec > >(tee -a /var/log/bairro-instalacao.log) 2>&1
if [[ ! -e .env ]]; then
    umask 077
    {
        printf 'DB_PASSWORD=%s\n' "$(openssl rand -hex 24)"
        printf 'JWT_SECRET=%s\n' "$(openssl rand -hex 48)"
        printf 'SEED_ENABLED=true\n'
        printf 'GESTOR_EMAIL=gestor@bairro.test\n'
        printf 'GESTOR_SENHA=%s\n' "$(openssl rand -hex 12)"
        printf 'CIDADAO_EMAIL=cidadao@bairro.test\n'
        printf 'CIDADAO_SENHA=%s\n' "$(openssl rand -hex 12)"
        printf 'CORS_ORIGINS=http://localhost:3000\n'
    } > .env
fi
chmod 600 .env
docker compose config --quiet
docker compose up -d --build
for tentativa in $(seq 1 90); do
    if curl -fsS http://127.0.0.1:8080/v3/api-docs -o /dev/null; then
        printf 'API pronta. Configuração privada em /opt/bairro/.env\n'
        exit 0
    fi
    sleep 5
done
docker compose logs --tail=80 api
exit 1
