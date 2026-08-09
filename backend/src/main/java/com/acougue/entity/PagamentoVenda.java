package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos_venda")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PagamentoVenda {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private String formaPagamento; // DINHEIRO, CREDITO, DEBITO, PIX, FIADO, CONVENIO, VOUCHER

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(length = 20)
    private String nsu;

    @Column(length = 20)
    private String autorizacao;

    @Column(name = "data_pagamento")
    @CreationTimestamp
    private LocalDateTime dataPagamento;
}
