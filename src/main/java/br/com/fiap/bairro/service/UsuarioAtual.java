package br.com.fiap.bairro.service;

import br.com.fiap.bairro.domain.*;
import br.com.fiap.bairro.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class UsuarioAtual {
    private final UsuarioRepository usuarios;

    public UsuarioAtual(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario obter() {
        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("Autenticação necessária.");
        }
        return usuarios.findById(Long.valueOf(jwt.getSubject()))
            .orElseThrow(() -> new AccessDeniedException("Usuário indisponível."));
    }

    public boolean gestor() {
        return obter().getPapel() == Papel.GESTOR;
    }
}
