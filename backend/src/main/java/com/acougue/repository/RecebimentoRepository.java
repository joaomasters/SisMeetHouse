package com.acougue.repository;

import com.acougue.entity.RecebimentoMercadoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecebimentoRepository extends JpaRepository<RecebimentoMercadoria, Long> {
    List<RecebimentoMercadoria> findAllByOrderByCreatedAtDesc();
    List<RecebimentoMercadoria> findByFornecedorContainingIgnoreCaseOrderByCreatedAtDesc(String fornecedor);

    @Query("SELECT r FROM RecebimentoMercadoria r WHERE r.dataRecebimento BETWEEN :inicio AND :fim " +
            "AND (:fornecedor = '' OR LOWER(r.fornecedor) LIKE LOWER(CONCAT('%', :fornecedor, '%'))) " +
            "ORDER BY r.dataRecebimento DESC")
    List<RecebimentoMercadoria> buscarComFiltros(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fim") LocalDateTime fim,
                                                 @Param("fornecedor") String fornecedor);
}