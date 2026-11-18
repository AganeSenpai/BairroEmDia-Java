package br.com.fiap.bairro.service;

import br.com.fiap.bairro.domain.*;
import br.com.fiap.bairro.dto.Contratos.*;
import br.com.fiap.bairro.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;
    private final JwtEncoder encoder;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder senhas, JwtEncoder encoder) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.encoder = encoder;
    }

    @Transactional
    public UsuarioResposta cadastrar(Cadastro entrada) {
        String email = entrada.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmail(email)) {
            throw new RegraNegocioException("E-mail já cadastrado.");
        }
        if (entrada.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Senha deve ter no máximo 72 bytes em UTF-8.");
        }
        var usuario = new Usuario();
        usuario.setNome(entrada.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(senhas.encode(entrada.senha()));
        usuario.setPapel(Papel.CIDADAO);
        return UsuarioResposta.de(usuarios.save(usuario));
    }

    @Transactional(readOnly = true)
    public Token login(Login entrada) {
        var usuario = usuarios.findByEmail(entrada.email().trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos."));
        if (!senhas.matches(entrada.senha(), usuario.getSenha())) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }
        var agora = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("bairro-em-dia")
            .subject(usuario.getId().toString()).issuedAt(agora).expiresAt(agora.plusSeconds(3600))
            .claim("papel", usuario.getPapel().name()).build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new Token(token, "Bearer", 3600, UsuarioResposta.de(usuario));
    }
}
