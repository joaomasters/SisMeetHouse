package com.acougue.repository;

import com.acougue.entity.ContasAReceber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContasAReceberRepository extends JpaRepository<ContasAReceber, Long> {

    List<ContasAReceber> findByClienteIdAndStatusOrderByDataVencimentoAsc(Long clienteId, String status);

    List<ContasAReceber> findByClienteId(Long clienteId);

    List<ContasAReceber> findByDataVencimentoBeforeAndStatusIn(LocalDate data, List<String> statuses);

    @Query("SELECT COALESCE(SUM(c.valor - c.valorPago), 0) FROM ContasAReceber c " +
           "WHERE c.cliente.id = :clienteId AND c.status IN ('ABERTO', 'PARCIAL')")
    BigDecimal saldoAberto(@Param("clienteId") Long clienteId);
}
