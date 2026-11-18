package br.com.fiap.bairro.service;

import br.com.fiap.bairro.domain.*;
import br.com.fiap.bairro.dto.Contratos.*;
import br.com.fiap.bairro.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SolicitacaoService {
    private final SolicitacaoRepository solicitacoes;
    private final CategoriaService categorias;
    private final UsuarioAtual usuarioAtual;

    public SolicitacaoService(SolicitacaoRepository solicitacoes, CategoriaService categorias, UsuarioAtual usuarioAtual) {
        this.solicitacoes = solicitacoes;
        this.categorias = categorias;
        this.usuarioAtual = usuarioAtual;
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoResposta> listar(Long categoriaId, StatusSolicitacao status, String termo, Pageable pagina) {
        var usuario = usuarioAtual.obter();
        Long cidadaoId = usuario.getPapel() == Papel.GESTOR ? null : usuario.getId();
        return solicitacoes.findAll(SolicitacaoSpecifications.filtrar(cidadaoId, categoriaId, status, termo), pagina)
            .map(SolicitacaoResposta::de);
    }

    @Transactional(readOnly = true)
    public SolicitacaoResposta buscar(Long id) {
        return SolicitacaoResposta.de(acessivel(id));
    }

    public SolicitacaoResposta criar(SolicitacaoEntrada entrada) {
        var solicitacao = new Solicitacao();
        solicitacao.setCidadao(usuarioAtual.obter());
        solicitacao.setStatus(StatusSolicitacao.ABERTA);
        solicitacao.setPrioridade(Prioridade.MEDIA);
        solicitacao.setCriadaEm(Instant.now());
        preencher(solicitacao, entrada);
        return SolicitacaoResposta.de(solicitacoes.save(solicitacao));
    }

    public SolicitacaoResposta atualizar(Long id, SolicitacaoEntrada entrada) {
        var solicitacao = acessivel(id);
        exigirAberta(solicitacao);
        preencher(solicitacao, entrada);
        return SolicitacaoResposta.de(solicitacao);
    }

    public SolicitacaoResposta atender(Long id, AtendimentoEntrada entrada) {
        var solicitacao = acessivel(id);
        var atual = solicitacao.getStatus();
        var proximo = entrada.status();
        boolean permitida = atual == proximo
            || (atual == StatusSolicitacao.ABERTA && (proximo == StatusSolicitacao.EM_ATENDIMENTO || proximo == StatusSolicitacao.CANCELADA))
            || (atual == StatusSolicitacao.EM_ATENDIMENTO && (proximo == StatusSolicitacao.RESOLVIDA || proximo == StatusSolicitacao.CANCELADA));
        if (!permitida) {
            throw new RegraNegocioException("Transição de status inválida.");
        }
        solicitacao.setStatus(proximo);
        solicitacao.setPrioridade(entrada.prioridade());
        solicitacao.setAtualizadaEm(Instant.now());
        return SolicitacaoResposta.de(solicitacao);
    }

    public void excluir(Long id) {
        var solicitacao = acessivel(id);
        exigirAberta(solicitacao);
        solicitacoes.delete(solicitacao);
    }

    @Transactional(readOnly = true)
    public Resumo resumo() {
        var usuario = usuarioAtual.obter();
        Long id = usuario.getPapel() == Papel.GESTOR ? null : usuario.getId();
        long abertas = id == null ? solicitacoes.contarPorStatus(StatusSolicitacao.ABERTA, null)
            : solicitacoes.countByCidadaoIdAndStatus(id, StatusSolicitacao.ABERTA);
        return new Resumo(abertas, solicitacoes.contarPorStatus(StatusSolicitacao.EM_ATENDIMENTO, id),
            solicitacoes.contarPorStatus(StatusSolicitacao.RESOLVIDA, id),
            solicitacoes.contarPorStatus(StatusSolicitacao.CANCELADA, id));
    }

    private Solicitacao acessivel(Long id) {
        var solicitacao = solicitacoes.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));
        var usuario = usuarioAtual.obter();
        if (usuario.getPapel() != Papel.GESTOR && !solicitacao.getCidadao().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Você não tem acesso a esta solicitação.");
        }
        return solicitacao;
    }

    private void exigirAberta(Solicitacao solicitacao) {
        if (solicitacao.getStatus() != StatusSolicitacao.ABERTA) {
            throw new RegraNegocioException("Somente solicitações abertas podem ser editadas ou excluídas.");
        }
    }

    private void preencher(Solicitacao solicitacao, SolicitacaoEntrada entrada) {
        solicitacao.setTitulo(entrada.titulo().trim());
        solicitacao.setDescricao(entrada.descricao().trim());
        solicitacao.setEndereco(entrada.endereco().trim());
        solicitacao.setCategoria(categorias.entidade(entrada.categoriaId()));
        solicitacao.setAtualizadaEm(Instant.now());
    }
}
