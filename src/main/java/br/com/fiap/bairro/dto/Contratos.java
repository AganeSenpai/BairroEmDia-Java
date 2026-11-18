package br.com.fiap.bairro.dto;

import br.com.fiap.bairro.domain.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

public final class Contratos {
    private Contratos() {}

    public record Cadastro(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 8, max = 72) String senha) {}

    public record Login(@NotBlank @Email String email, @NotBlank String senha) {}
    public record Token(String accessToken, String tokenType, long expiresIn, UsuarioResposta usuario) {}
    public record UsuarioResposta(Long id, String nome, String email, Papel papel) {
        public static UsuarioResposta de(Usuario usuario) {
            return new UsuarioResposta(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel());
        }
    }

    public record CategoriaEntrada(
        @NotBlank @Size(max = 80) String nome,
        @NotBlank @Size(max = 300) String descricao) {}

    public record CategoriaResposta(Long id, String nome, String descricao) {
        public static CategoriaResposta de(Categoria categoria) {
            return new CategoriaResposta(categoria.getId(), categoria.getNome(), categoria.getDescricao());
        }
    }

    public record SolicitacaoEntrada(
        @NotBlank @Size(max = 120) String titulo,
        @NotBlank @Size(max = 2000) String descricao,
        @NotBlank @Size(max = 250) String endereco,
        @NotNull @Positive Long categoriaId) {}

    public record AtendimentoEntrada(@NotNull StatusSolicitacao status, @NotNull Prioridade prioridade) {}

    public record SolicitacaoResposta(Long id, String titulo, String descricao, String endereco,
        StatusSolicitacao status, Prioridade prioridade, Long cidadaoId, CategoriaResposta categoria,
        Instant criadaEm, Instant atualizadaEm) {
        public static SolicitacaoResposta de(Solicitacao solicitacao) {
            return new SolicitacaoResposta(solicitacao.getId(), solicitacao.getTitulo(),
                solicitacao.getDescricao(), solicitacao.getEndereco(), solicitacao.getStatus(),
                solicitacao.getPrioridade(), solicitacao.getCidadao().getId(),
                CategoriaResposta.de(solicitacao.getCategoria()), solicitacao.getCriadaEm(), solicitacao.getAtualizadaEm());
        }
    }

    public record Resumo(long abertas, long emAtendimento, long resolvidas, long canceladas) {}
}
