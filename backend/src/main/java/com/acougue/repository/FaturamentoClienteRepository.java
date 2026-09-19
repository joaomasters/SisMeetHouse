package com.acougue.repository;

import com.acougue.entity.FaturamentoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FaturamentoClienteRepository extends JpaRepository<FaturamentoCliente, Long> {

    List<FaturamentoCliente> findByClienteIdOrderByPeriodoFimDesc(Long clienteId);

    List<FaturamentoCliente> findByStatusIn(List<String> statuses);

    List<FaturamentoCliente> findByDataVencimentoBeforeAndStatusIn(
            LocalDate data, List<String> statuses
    );

    @Query("SELECT f FROM FaturamentoCliente f WHERE f.cliente.id = :clienteId " +
            "AND f.status IN ('ABERTO', 'PARCIAL', 'VENCIDO') " +
            "AND f.periodoInicio <= :fim AND f.periodoFim >= :inicio")
    List<FaturamentoCliente> buscarSobrepostos(@Param("clienteId") Long clienteId,
                                               @Param("inicio") LocalDate inicio,
                                               @Param("fim") LocalDate fim);
}