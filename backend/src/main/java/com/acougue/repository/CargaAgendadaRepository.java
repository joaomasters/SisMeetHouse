package com.acougue.repository;

import com.acougue.entity.CargaAgendada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CargaAgendadaRepository extends JpaRepository<CargaAgendada, Long> {

    Optional<CargaAgendada> findFirstByStatusOrderByCriadoEmAsc(String status);

    List<CargaAgendada> findTop20ByOrderByCriadoEmDesc();

    @Query("SELECT MAX(c.criadoEm) FROM CargaAgendada c WHERE c.status = 'APLICADA'")
    Optional<LocalDateTime> findUltimaCargaAplicada();
}
