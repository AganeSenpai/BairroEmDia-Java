package br.com.fiap.bairro.web;

import java.util.Set;
import org.springframework.data.domain.*;

public final class Paginacao {
    private Paginacao() {}

    public static Pageable validar(Pageable pagina, Set<String> campos) {
        for (Sort.Order ordem : pagina.getSort()) {
            if (!campos.contains(ordem.getProperty())) {
                throw new IllegalArgumentException("Campo de ordenação inválido. Permitidos: " + campos);
            }
        }
        return PageRequest.of(pagina.getPageNumber(), Math.min(pagina.getPageSize(), 100), pagina.getSort());
    }
}
