package br.com.fiap.bairro.repository;

import br.com.fiap.bairro.domain.*;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class SolicitacaoSpecifications {
    private SolicitacaoSpecifications() {}

    public static Specification<Solicitacao> filtrar(Long cidadaoId, Long categoriaId,
                                                    StatusSolicitacao status, String termo) {
        Specification<Solicitacao> filtro = (root, query, cb) -> cb.conjunction();
        if (cidadaoId != null) {
            filtro = filtro.and((root, query, cb) -> cb.equal(root.get("cidadao").get("id"), cidadaoId));
        }
        if (categoriaId != null) {
            filtro = filtro.and((root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId));
        }
        if (status != null) {
            filtro = filtro.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (termo != null && !termo.isBlank()) {
            String literal = termo.trim().toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_");
            filtro = filtro.and((root, query, cb) -> cb.like(cb.lower(root.get("titulo")), "%" + literal + "%", '!'));
        }
        return filtro;
    }
}
