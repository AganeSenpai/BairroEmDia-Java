# Roteiro da demonstração DevOps

Grave a tela em 1920×1080, com áudio do microfone. Aumente o terminal e o zoom do navegador para que comandos e respostas sejam legíveis. Assista à gravação antes de enviar. Não mostre o arquivo `.env` nem tokens.

| Trecho | Mostrar | Explicação sugerida |
|---|---|---|
| Solução | Diagrama de `docs/DEVOPS.md` | O Bairro em Dia registra solicitações de zeladoria urbana. A API Java recebe os pedidos e o PostgreSQL mantém os dados. |
| Provisionamento | Script e evidência original de `az vm create` | A máquina Ubuntu foi criada com Azure CLI. O script também configura a regra da porta 8080 e instala o ambiente. |
| Repositório | Dockerfile, Compose e pasta devops | O Dockerfile compila a API e a executa com usuário app. O Compose define os dois containers, a rede e o volume persistente. |
| Ambiente | `bash devops/coletar-evidencias.sh` no Cloud Shell | Os containers estão ativos. O banco está saudável. A API executa em /app, sem usuário root, e se comunica com o banco pela rede Docker. |
| CRUD | Script de demonstração e respostas HTTP | A demonstração autentica os usuários e cria, consulta, altera e exclui categoria e solicitação. Respostas 201 e 204 confirmam criação e exclusão; 404 confirma que o item excluído não está mais disponível. |
| Persistência | Evidência de recriação e SELECT | Os identificadores dos containers mudam, mas a consulta retorna a mesma solicitação. O volume do PostgreSQL preservou os dados. |
| Encerramento | GitHub e resultado final | O código, a configuração dos containers, os scripts e as evidências estão no repositório. |

No Cloud Shell, a partir do repositório atualizado:

```bash
az vm run-command invoke -g rg-bairro-em-dia -n vm-bairro-em-dia \
  --command-id RunShellScript \
  --scripts 'cd /opt/bairro && python3 devops/demonstrar-crud.py' \
  --query 'value[].message' -o tsv

az vm run-command invoke -g rg-bairro-em-dia -n vm-bairro-em-dia \
  --command-id RunShellScript \
  --scripts 'bash /opt/bairro/devops/demonstrar-persistencia.sh' \
  --query 'value[].message' -o tsv
```

O segundo comando recria os containers e causa uma breve indisponibilidade da demonstração. Ele preserva o volume. Execute após concluir o CRUD. A evidência original de criação da VM deve ser mostrada sem recriar a VM durante a gravação.

A gravação e seu link ainda precisam ser adicionados à entrega pelo integrante.
