package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacao_estoque", indexes = {
    @Index(name = "idx_mov_produto_data", columnList = "produto_id, created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MovimentacaoEstoque {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "tipo_movimentacao", nullable = false, length = 30)
    private String tipoMovimentacao;
    // ENTRADA_COMPRA, ENTRADA_DESOSSA, SAIDA_VENDA,
    // SAIDA_DESOSSA, SAIDA_DESCARTE, SAIDA_QUEBRA,
    // SAIDA_MOAGEM, AJUSTE_POSITIVO, AJUSTE_NEGATIVO

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "custo_unitario", precision = 12, scale = 4)
    private BigDecimal custoUnitario;

    @Column(name = "documento_ref", length = 50)
    private String documentoRef;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
