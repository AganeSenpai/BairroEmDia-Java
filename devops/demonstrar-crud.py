import json
import os
import sys
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

base_url = 'http://127.0.0.1:8080'
configuracao = {}
for linha in Path('/opt/bairro/.env').read_text().splitlines():
    if '=' in linha:
        chave, valor = linha.split('=', 1)
        configuracao[chave] = valor


def requisicao(metodo, caminho, dados=None, token=None, esperado=200):
    cabecalhos = {'Content-Type': 'application/json'}
    if token:
        cabecalhos['Authorization'] = 'Bearer ' + token
    corpo = None if dados is None else json.dumps(dados).encode()
    pedido = urllib.request.Request(base_url + caminho, data=corpo,
                                    headers=cabecalhos, method=metodo)
    try:
        with urllib.request.urlopen(pedido, timeout=30) as resposta:
            status = resposta.status
            conteudo = resposta.read()
    except urllib.error.HTTPError as erro:
        status = erro.code
        conteudo = erro.read()
    resultado = json.loads(conteudo) if conteudo else None
    print(f'{metodo} {caminho} -> HTTP {status}', flush=True)
    if status != esperado:
        raise RuntimeError(f'Resposta inesperada: {resultado}')
    return resultado


def entrar(papel):
    resultado = requisicao('POST', '/api/auth/login', {
        'email': configuracao[papel + '_EMAIL'],
        'senha': configuracao[papel + '_SENHA'],
    })
    return resultado['accessToken']


def main():
    if '--verificar-persistencia' in sys.argv:
        cidadao = entrar('CIDADAO')
        identificador = Path('/opt/bairro/persistencia-id.txt').read_text().strip()
        registro = requisicao('GET', '/api/solicitacoes/' + identificador, token=cidadao)
        if registro['titulo'] != 'Registro para comprovar persistencia':
            raise RuntimeError('Conteudo persistido divergente.')
        print('PERSISTENCIA CONFIRMADA: id=' + str(registro['id']) + ', titulo=' + registro['titulo'])
        return
    gestor = entrar('GESTOR')
    cidadao = entrar('CIDADAO')
    nome = 'Demonstracao ' + datetime.now(timezone.utc).strftime('%Y%m%d%H%M%S')
    categoria = requisicao('POST', '/api/categorias',
                          {'nome': nome, 'descricao': 'Categoria para evidenciar o CRUD.'}, gestor, 201)
    categoria_id = categoria['id']
    requisicao('GET', f'/api/categorias/{categoria_id}', token=gestor)
    requisicao('PUT', f'/api/categorias/{categoria_id}',
               {'nome': nome, 'descricao': 'Descricao atualizada pela API.'}, gestor)
    entrada = {'titulo': 'Poste da demonstracao', 'descricao': 'Registro ficticio para a avaliacao.',
               'endereco': 'Rua de Demonstracao, 100 - Centro', 'categoriaId': categoria_id}
    solicitacao = requisicao('POST', '/api/solicitacoes', entrada, cidadao, 201)
    solicitacao_id = solicitacao['id']
    requisicao('GET', f'/api/solicitacoes/{solicitacao_id}', token=cidadao)
    entrada['titulo'] = 'Poste da demonstracao atualizado'
    requisicao('PUT', f'/api/solicitacoes/{solicitacao_id}', entrada, cidadao)
    requisicao('DELETE', f'/api/solicitacoes/{solicitacao_id}', token=cidadao, esperado=204)
    requisicao('GET', f'/api/solicitacoes/{solicitacao_id}', token=cidadao, esperado=404)
    requisicao('DELETE', f'/api/categorias/{categoria_id}', token=gestor, esperado=204)
    requisicao('GET', f'/api/categorias/{categoria_id}', token=gestor, esperado=404)
    categorias = requisicao('GET', '/api/categorias', token=gestor)
    categoria_existente = categorias['_embedded']['categoriaRespostaList'][0]['id']
    entrada['categoriaId'] = categoria_existente
    entrada['titulo'] = 'Registro para comprovar persistencia'
    persistida = requisicao('POST', '/api/solicitacoes', entrada, cidadao, 201)
    Path('/opt/bairro/persistencia-id.txt').write_text(str(persistida['id']))
    os.chmod('/opt/bairro/persistencia-id.txt', 0o600)
    print('REGISTRO_PERSISTENCIA=' + str(persistida['id']), flush=True)


if __name__ == '__main__':
    main()
