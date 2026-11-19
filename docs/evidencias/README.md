# Evidências de DevOps

Cada evidência deve registrar uma execução real e indicar o comando e o resultado.

| Etapa | Arquivo esperado | O que comprova |
|---|---|---|
| Provisionamento | 01-provisionamento-cli.log e captura | `az group create`, `az vm create`, resultado e configuração da VM |
| VM | 02-vm.json e captura | Nome, região, tamanho, estado e endereço da VM |
| Containers | 03-ambiente.log e capturas | Compose, logs, usuário, diretório, rede, volume e SELECT |
| CRUD | 04-crud.log e capturas | Criação, leitura, alteração e exclusão na API publicada |
| Persistência | 05-persistencia.log e captura | Mesmo registro consultado depois de recriar os containers |
| GitHub | Capturas do repositório e commits | Código-fonte, Dockerfile, Compose e script Azure CLI disponíveis |

As evidências de execução na Azure serão adicionadas após o provisionamento. A existência de um script, por si só, não comprova que ele foi executado.
