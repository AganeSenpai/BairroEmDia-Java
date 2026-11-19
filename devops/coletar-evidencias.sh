#!/usr/bin/env bash
set -Eeuo pipefail
cd "$(dirname "$0")/.."
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-bairro-em-dia}"
VM_NAME="${VM_NAME:-vm-bairro-em-dia}"
EVIDENCE_DIR="${EVIDENCE_DIR:-$HOME/bairro-evidencias}"
mkdir -p "$EVIDENCE_DIR"
chmod 700 "$EVIDENCE_DIR"
az vm show -g "$RESOURCE_GROUP" -n "$VM_NAME" -d \
    --query '{nome:name,estado:powerState,regiao:location,tamanho:hardwareProfile.vmSize,ip:publicIps}' -o json \
    | tee "$EVIDENCE_DIR/02-vm.json"
az vm run-command invoke -g "$RESOURCE_GROUP" -n "$VM_NAME" --command-id RunShellScript \
    --scripts @devops/verificar-ambiente.sh --query 'value[].message' -o tsv \
    | tee "$EVIDENCE_DIR/03-ambiente.log"
printf '\nColeta do ambiente concluída. Demonstre o CRUD e a persistência antes de finalizar a entrega.\n'
