package com.acougue.repository;

import com.acougue.entity.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByProdutoIdOrderByCreatedAtDesc(Long produtoId);

    @Query("SELECT m FROM MovimentacaoEstoque m WHERE m.produto.id = :produtoId " +
           "AND m.createdAt BETWEEN :inicio AND :fim ORDER BY m.createdAt DESC")
    List<MovimentacaoEstoque> findByProdutoIdAndPeriodo(
        @Param("produtoId") Long produtoId,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT m FROM MovimentacaoEstoque m WHERE m.createdAt BETWEEN :inicio AND :fim " +
           "ORDER BY m.createdAt DESC")
    List<MovimentacaoEstoque> findByPeriodo(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
}
