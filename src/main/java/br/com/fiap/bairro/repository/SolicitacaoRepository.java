package br.com.fiap.bairro.repository;

import br.com.fiap.bairro.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long>, JpaSpecificationExecutor<Solicitacao> {
    long countByCidadaoIdAndStatus(Long cidadaoId, StatusSolicitacao status);
    boolean existsByCategoriaId(Long categoriaId);

    @Query("select count(s) from Solicitacao s where s.status = :status and (:cidadaoId is null or s.cidadao.id = :cidadaoId)")
    long contarPorStatus(@Param("status") StatusSolicitacao status, @Param("cidadaoId") Long cidadaoId);
}
