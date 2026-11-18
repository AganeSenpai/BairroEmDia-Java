package br.com.fiap.bairro.config;

import br.com.fiap.bairro.domain.*;
import br.com.fiap.bairro.repository.*;
import java.time.Instant;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DadosDemonstracao implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final SolicitacaoRepository solicitacoes;
    private final PasswordEncoder senhas;
    private final String gestorEmail;
    private final String gestorSenha;
    private final String cidadaoEmail;
    private final String cidadaoSenha;

    public DadosDemonstracao(UsuarioRepository usuarios, CategoriaRepository categorias,
        SolicitacaoRepository solicitacoes, PasswordEncoder senhas,
        @Value("${app.seed.gestor-email}") String gestorEmail,
        @Value("${app.seed.gestor-senha}") String gestorSenha,
        @Value("${app.seed.cidadao-email}") String cidadaoEmail,
        @Value("${app.seed.cidadao-senha}") String cidadaoSenha) {
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.solicitacoes = solicitacoes;
        this.senhas = senhas;
        this.gestorEmail = gestorEmail;
        this.gestorSenha = gestorSenha;
        this.cidadaoEmail = cidadaoEmail;
        this.cidadaoSenha = cidadaoSenha;
    }

    @Override
    @Transactional
    public void run(String... args) {
        criarUsuario("Gestor de demonstração", gestorEmail, gestorSenha, Papel.GESTOR);
        var cidadao = criarUsuario("Cidadão de demonstração", cidadaoEmail, cidadaoSenha, Papel.CIDADAO);
        if (categorias.count() == 0) {
            criarCategoria("Iluminação", "Postes apagados e iluminação de espaços públicos.");
            criarCategoria("Vias públicas", "Buracos e problemas em ruas e calçadas.");
            criarCategoria("Limpeza urbana", "Descarte irregular e limpeza de espaços públicos.");
        }
        if (solicitacoes.count() == 0) {
            var solicitacao = new Solicitacao();
            solicitacao.setTitulo("Poste apagado na praça");
            solicitacao.setDescricao("Iluminação apagada há três noites no acesso à praça.");
            solicitacao.setEndereco("Rua Exemplo, 100 - Centro (endereço fictício)");
            solicitacao.setCategoria(categorias.findAll().getFirst());
            solicitacao.setCidadao(cidadao);
            solicitacao.setStatus(StatusSolicitacao.ABERTA);
            solicitacao.setPrioridade(Prioridade.MEDIA);
            solicitacao.setCriadaEm(Instant.now());
            solicitacao.setAtualizadaEm(Instant.now());
            solicitacoes.save(solicitacao);
        }
    }

    private Usuario criarUsuario(String nome, String email, String senha, Papel papel) {
        String normalizado = email.trim().toLowerCase(Locale.ROOT);
        return usuarios.findByEmail(normalizado).orElseGet(() -> {
            if (senha.length() < 8 || senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
                throw new IllegalArgumentException("Configure as senhas de demonstração com 8 caracteres e até 72 bytes.");
            }
            var usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setEmail(normalizado);
            usuario.setSenha(senhas.encode(senha));
            usuario.setPapel(papel);
            return usuarios.save(usuario);
        });
    }

    private void criarCategoria(String nome, String descricao) {
        var categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setDescricao(descricao);
        categorias.save(categoria);
    }
}
