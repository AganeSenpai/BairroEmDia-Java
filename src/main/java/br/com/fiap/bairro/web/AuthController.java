package br.com.fiap.bairro.web;

import br.com.fiap.bairro.dto.Contratos.*;
import br.com.fiap.bairro.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {
    private final AuthService auth;
    private final UsuarioAtual usuario;

    public AuthController(AuthService auth, UsuarioAtual usuario) {
        this.auth = auth;
        this.usuario = usuario;
    }

    @PostMapping("/cadastro")
    @Operation(summary = "Cadastrar cidadão", description = "O cadastro público sempre cria o papel CIDADAO.")
    public ResponseEntity<UsuarioResposta> cadastrar(@Valid @RequestBody Cadastro entrada) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.cadastrar(entrada));
    }

    @PostMapping("/login")
    @Operation(summary = "Entrar e obter token JWT com validade de uma hora")
    public Token login(@Valid @RequestBody Login entrada) {
        return auth.login(entrada);
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar usuário autenticado")
    public UsuarioResposta atual() {
        return UsuarioResposta.de(usuario.obter());
    }
}
