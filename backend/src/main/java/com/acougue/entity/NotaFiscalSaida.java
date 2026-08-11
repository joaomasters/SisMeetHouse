package com.acougue.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nota_fiscal_saida")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotaFiscalSaida {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_nf", length = 50)
    private String numeroNf;

    @Column(name = "serie_nf", length = 5)
    @Builder.Default
    private String serieNf = "1";

    @Column(name = "chave_nf", length = 44)
    private String chaveNf;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "natureza_operacao", nullable = false, length = 200)
    @Builder.Default
    private String naturezaOperacao = "VENDA DE MERCADORIAS";

    @Column(name = "data_emissao")
    @CreationTimestamp
    private LocalDateTime dataEmissao;

    @Column(name = "valor_produtos", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorProdutos = BigDecimal.ZERO;

    @Column(name = "valor_desconto", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_total", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Builder.Default
    private String status = "PENDENTE";

    @Column(name = "xml_nf", columnDefinition = "TEXT")
    private String xmlNf;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @OneToMany(mappedBy = "nota", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NotaFiscalSaidaItem> itens = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
