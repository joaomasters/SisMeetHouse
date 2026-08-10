package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_fisico_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventarioFisicoItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_id", nullable = false)
    private InventarioFisico inventario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "saldo_sistema", nullable = false, precision = 12, scale = 4)
    private BigDecimal saldoSistema;

    @Column(name = "saldo_contado", precision = 12, scale = 4)
    private BigDecimal saldoContado;

    @Column(precision = 12, scale = 4)
    private BigDecimal divergencia;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
