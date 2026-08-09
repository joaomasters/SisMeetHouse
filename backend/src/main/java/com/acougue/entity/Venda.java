package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendas", indexes = {
    @Index(name = "idx_vendas_data_status", columnList = "data_venda, status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Venda {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cupom", length = 20)
    private String numeroCupom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "tipo_venda", nullable = false, length = 20)
    private String tipoVenda; // PDV, FATURAMENTO, DELIVERY

    @Builder.Default
    private String status = "ABERTA"; // ABERTA, FECHADA, CANCELADA

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal troco = BigDecimal.ZERO;

    @Column(name = "data_venda")
    @CreationTimestamp
    private LocalDateTime dataVenda;

    @Column(name = "operador_id")
    private Long operadorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caixa_id")
    private Caixa caixa;

    @Column(name = "nfce_chave", length = 44)
    private String nfceChave;

    @Column(name = "nfce_status", length = 20)
    private String nfceStatus;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItensVenda> itens = new ArrayList<>();

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PagamentoVenda> pagamentos = new ArrayList<>();
}
