#!/usr/bin/env bash
set -Eeuo pipefail
cd "$(dirname "$0")/.."
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-bairro-em-dia}"
VM_NAME="${VM_NAME:-vm-bairro-em-dia}"
LOCATION="${LOCATION:-brazilsouth}"
VM_SIZE="${VM_SIZE:-Standard_D2s_v4}"
ADMIN_USER="${ADMIN_USER:-bairroadmin}"
EVIDENCE_DIR="${EVIDENCE_DIR:-$HOME/bairro-evidencias}"
mkdir -p "$EVIDENCE_DIR"
chmod 700 "$EVIDENCE_DIR"
exec > >(tee -a "$EVIDENCE_DIR/01-provisionamento-cli.log") 2>&1
registrar() {
    printf '\n$'
    printf ' %q' "$@"
    printf '\n'
    "$@"
}
az account show --query name -o tsv
if [[ -n "${SUBSCRIPTION_ID:-}" ]]; then
    az account set --subscription "$SUBSCRIPTION_ID"
fi
registrar az version --query '"azure-cli"' -o tsv
if [[ $(az group exists --name "$RESOURCE_GROUP" -o tsv) != true ]]; then
    registrar az group create --name "$RESOURCE_GROUP" --location "$LOCATION" -o none
fi
registrar az group show --name "$RESOURCE_GROUP" --query '{nome:name,regiao:location,estado:properties.provisioningState}' -o json
if az vm show --resource-group "$RESOURCE_GROUP" --name "$VM_NAME" -o none 2>/dev/null; then
    printf 'VM existente encontrada. A evidência original de criação deve ser preservada.\n'
else
    registrar az vm create --resource-group "$RESOURCE_GROUP" --name "$VM_NAME" \
        --location "$LOCATION" --image Ubuntu2204 --size "$VM_SIZE" \
        --admin-username "$ADMIN_USER" --generate-ssh-keys --authentication-type ssh \
        --nsg-rule NONE --public-ip-sku Standard --storage-sku Standard_LRS \
        --os-disk-size-gb 32 --tags projeto=BairroEmDia disciplina=DevOps \
        --query '{nome:powerState,ip:publicIpAddress,grupo:resourceGroup,local:location}' -o json
fi
NSG_NAME=$(az network nic show --ids "$(az vm show -g "$RESOURCE_GROUP" -n "$VM_NAME" --query 'networkProfile.networkInterfaces[0].id' -o tsv)" --query 'networkSecurityGroup.id' -o tsv)
NSG_NAME="${NSG_NAME##*/}"
registrar az network nsg rule create --resource-group "$RESOURCE_GROUP" --nsg-name "$NSG_NAME" --name permitir-api \
    --priority 1000 --direction Inbound --access Allow --protocol Tcp \
    --source-address-prefixes Internet --destination-port-ranges 8080 \
    --query '{nome:name,porta:destinationPortRange,acesso:access}' -o json
registrar az vm show -g "$RESOURCE_GROUP" -n "$VM_NAME" -d \
    --query '{nome:name,estado:powerState,regiao:location,tamanho:hardwareProfile.vmSize,ip:publicIps}' -o json
registrar az vm run-command invoke -g "$RESOURCE_GROUP" -n "$VM_NAME" \
    --command-id RunShellScript --scripts @devops/instalar-docker.sh \
    --query 'value[].message' -o tsv
printf '\nProvisionamento finalizado. Registre o arquivo 01-provisionamento-cli.log e as capturas do terminal.\n'
