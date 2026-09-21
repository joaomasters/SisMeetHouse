package com.acougue.repository;

import com.acougue.entity.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {

    /**
     * Todos os filtros são opcionais (passe null pra ignorar) — evita
     * precisar de várias queries derivadas para cada combinação possível.
     */
    @Query("SELECT l FROM LogAuditoria l WHERE l.criadoEm BETWEEN :inicio AND :fim " +
            "AND (:modulo IS NULL OR l.modulo = :modulo) " +
            "AND (:acao IS NULL OR l.acao = :acao) " +
            "AND (:usuarioId IS NULL OR l.usuarioId = :usuarioId) " +
            "ORDER BY l.criadoEm DESC")
    List<LogAuditoria> buscarComFiltros(@Param("inicio") LocalDateTime inicio,
                                        @Param("fim") LocalDateTime fim,
                                        @Param("modulo") String modulo,
                                        @Param("acao") String acao,
                                        @Param("usuarioId") Long usuarioId);
}