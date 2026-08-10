package com.acougue.repository;

import com.acougue.entity.PerdasEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PerdasEstoqueRepository extends JpaRepository<PerdasEstoque, Long> {

    @Query("SELECT p FROM PerdasEstoque p WHERE p.createdAt BETWEEN :inicio AND :fim ORDER BY p.createdAt DESC")
    List<PerdasEstoque> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    List<PerdasEstoque> findByProdutoIdOrderByCreatedAtDesc(Long produtoId);
}
