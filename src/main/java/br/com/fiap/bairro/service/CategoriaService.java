package br.com.fiap.bairro.service;

import br.com.fiap.bairro.domain.Categoria;
import br.com.fiap.bairro.dto.Contratos.*;
import br.com.fiap.bairro.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final SolicitacaoRepository solicitacoes;

    public CategoriaService(CategoriaRepository categorias, SolicitacaoRepository solicitacoes) {
        this.categorias = categorias;
        this.solicitacoes = solicitacoes;
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResposta> listar(Pageable pagina) {
        return categorias.findAll(pagina).map(CategoriaResposta::de);
    }

    @Transactional(readOnly = true)
    public CategoriaResposta buscar(Long id) {
        return CategoriaResposta.de(entidade(id));
    }

    public CategoriaResposta criar(CategoriaEntrada entrada) {
        if (categorias.existsByNomeIgnoreCase(entrada.nome().trim())) {
            throw new RegraNegocioException("Já existe uma categoria com esse nome.");
        }
        var categoria = new Categoria();
        preencher(categoria, entrada);
        return CategoriaResposta.de(categorias.save(categoria));
    }

    public CategoriaResposta atualizar(Long id, CategoriaEntrada entrada) {
        var categoria = entidade(id);
        if (!categoria.getNome().equalsIgnoreCase(entrada.nome().trim())
                && categorias.existsByNomeIgnoreCase(entrada.nome().trim())) {
            throw new RegraNegocioException("Já existe uma categoria com esse nome.");
        }
        preencher(categoria, entrada);
        return CategoriaResposta.de(categoria);
    }

    public void excluir(Long id) {
        var categoria = entidade(id);
        if (solicitacoes.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Categoria possui solicitações vinculadas.");
        }
        categorias.delete(categoria);
    }

    public Categoria entidade(Long id) {
        return categorias.findById(id).orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada."));
    }

    private void preencher(Categoria categoria, CategoriaEntrada entrada) {
        categoria.setNome(entrada.nome().trim());
        categoria.setDescricao(entrada.descricao().trim());
    }
}
