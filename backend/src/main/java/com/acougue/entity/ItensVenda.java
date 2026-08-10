package com.acougue.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_venda", indexes = {
    @Index(name = "idx_itens_venda_venda", columnList = "venda_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItensVenda {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "ean13_lido", length = 13)
    private String ean13Lido;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal precoUnitario;

    @Column(name = "desconto_item", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal descontoItem = BigDecimal.ZERO;

    @Column(name = "total_item", nullable = false, precision = 12, scale = 4)
    private BigDecimal totalItem;

    @Column(name = "custo_item", precision = 12, scale = 4)
    private BigDecimal custoItem;

    @Column(name = "tipo_entrada", length = 20)
    private String tipoEntrada; // BARCODE, PESAGEM_DIRETA, MANUAL
}
