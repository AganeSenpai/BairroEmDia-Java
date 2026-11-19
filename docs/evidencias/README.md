# Evidências de DevOps

Execuções reais de 19/11/2026 na VM `vm-bairro-em-dia`, Azure Brazil South, `Standard_D2s_v4`. O grupo de recursos foi criado inicialmente em East US 2; a VM foi criada em Brazil South, região permitida pela assinatura.

| Verificação | Registro | Captura |
|---|---|---|
| Criação por Azure CLI, NSG e instalação | [Transcript](01-provisionamento-cli.log.txt) | [Comando e resultado](02_azure_provisionamento.jpg) |
| Configuração da VM | [VM](02-vm.json) | [Provisionamento](02_azure_provisionamento.jpg) |
| Containers, usuário app, diretório /app | [Ambiente](03-ambiente.log.txt) | `06_containers_azure.jpg` no pacote de evidências |
| Rede, volume, logs e SELECT | [Ambiente](03-ambiente.log.txt) | `07_rede_volume_select.jpg` no pacote de evidências |
| CRUD de categoria e solicitação | [Respostas HTTP](04-crud.log.txt) | [CRUD](03_crud_azure.jpg) |
| Persistência após recriar containers | [Antes e depois](05-persistencia.log.txt) | `04_persistencia_azure.jpg` no pacote de evidências |
| Swagger pelo endereço público | [Rotas e versão](03-ambiente.log.txt) | `05_swagger_publico.jpg` no pacote de evidências |
| Dockerfile, Compose e scripts no GitHub | [Código e histórico](../../) | `08_github_arquivos.jpg` no pacote de evidências |

O CRUD criou, consultou, alterou e excluiu registros, com HTTP 201, 200 e 204. As consultas após exclusão retornaram 404. O registro de persistência ID 3 retornou HTTP 200 antes e depois da recriação e foi confirmado por SELECT. Os containers tiveram seus identificadores alterados; o volume permaneceu.

Os arquivos `.log.txt` preservam os registros capturados do terminal. O caminho pessoal do diretório home e eventuais identificadores de assinatura foram omitidos nas cópias públicas. Nenhuma senha ou token faz parte das evidências. O transcript público de provisionamento é um recorte da criação bem-sucedida, sem os detalhes das tentativas iniciais.

A gravação ainda precisa ser feita pelo integrante, em 720p ou superior, com narração ou legendas, seguindo o [roteiro](../ROTEIRO-DEVOPS.md).

As demais capturas foram reunidas no pacote de evidências entregue ao integrante.
