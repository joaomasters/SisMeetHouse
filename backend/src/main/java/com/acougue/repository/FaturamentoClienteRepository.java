package com.acougue.repository;

import com.acougue.entity.FaturamentoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
