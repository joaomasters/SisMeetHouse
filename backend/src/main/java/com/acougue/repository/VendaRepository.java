package com.acougue.repository;

import com.acougue.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findByClienteIdOrderByDataVendaDesc(Long clienteId);

    List<Venda> findByCaixaIdAndStatus(Long caixaId, String status);

    @Query("SELECT v FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim ORDER BY v.dataVenda DESC")
    List<Venda> findByPeriodo(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT v FROM Venda v WHERE v.cliente.id = :clienteId " +
           "AND v.status = 'FECHADA' AND v.dataVenda BETWEEN :inicio AND :fim")
    List<Venda> findVendasFaturamentoCliente(
        @Param("clienteId") Long clienteId,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venda v " +
           "WHERE v.dataVenda BETWEEN :inicio AND :fim AND v.status = 'FECHADA'")
    BigDecimal somarTotalPeriodo(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
}
