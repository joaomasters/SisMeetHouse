package com.acougue.repository;

import com.acougue.entity.NotaFiscalSaida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaFiscalSaidaRepository extends JpaRepository<NotaFiscalSaida, Long> {
    List<NotaFiscalSaida> findAllByOrderByCreatedAtDesc();
    List<NotaFiscalSaida> findByClienteIdOrderByCreatedAtDesc(Long clienteId);
}
