package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contas_a_receber", indexes = {
    @Index(name = "idx_contas_cliente_status", columnList = "cliente_id, status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContasAReceber {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faturamento_id")
    private FaturamentoCliente faturamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @Column(length = 200)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "valor_pago", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Builder.Default
    private String status = "ABERTO"; // ABERTO, PARCIAL, PAGO, CANCELADO

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
