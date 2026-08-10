package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contas_pagar")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContasPagar {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(length = 255)
    private String fornecedor;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Builder.Default
    @Column(name = "valor_pago", precision = 12, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(length = 100)
    private String categoria;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "ABERTO"; // ABERTO, PAGO, PARCIAL, CANCELADO

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
