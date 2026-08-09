package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "faturamento_cliente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FaturamentoCliente {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fim", nullable = false)
    private LocalDate periodoFim;

    @Column(name = "total_vendas", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalVendas = BigDecimal.ZERO;

    @Column(name = "total_pago", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPago = BigDecimal.ZERO;

    @Column(name = "saldo_devedor", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal saldoDevedor = BigDecimal.ZERO;

    @Builder.Default
    private String status = "ABERTO"; // ABERTO, PARCIAL, QUITADO, VENCIDO

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "nfe_chave", length = 44)
    private String nfeChave;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
