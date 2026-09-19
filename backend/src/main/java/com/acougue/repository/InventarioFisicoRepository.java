package com.acougue.repository;

import com.acougue.entity.InventarioFisico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioFisicoRepository extends JpaRepository<InventarioFisico, Long> {
    Optional<InventarioFisico> findFirstByStatusOrderByCreatedAtDesc(String status);
    List<InventarioFisico> findAllByOrderByCreatedAtDesc();

    @Query("SELECT i FROM InventarioFisico i WHERE i.createdAt BETWEEN :inicio AND :fim ORDER BY i.createdAt DESC")
    List<InventarioFisico> buscarPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}