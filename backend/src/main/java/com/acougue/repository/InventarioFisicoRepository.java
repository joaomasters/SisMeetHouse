package com.acougue.repository;

import com.acougue.entity.InventarioFisico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioFisicoRepository extends JpaRepository<InventarioFisico, Long> {
    Optional<InventarioFisico> findFirstByStatusOrderByCreatedAtDesc(String status);
    List<InventarioFisico> findAllByOrderByCreatedAtDesc();
}
