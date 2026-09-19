package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa um produto cujo preço de venda mudou e ainda não foi
 * carregado na balança física. Criado/atualizado automaticamente por
 * {@code ItemPendenteBalancaService} sempre que
 * {@code ProdutoService.atualizar()} detecta mudança em precoVenda
 * para um produto com codigoBalanca (PLU) cadastrado.
 */
@Entity
@Table(name = "item_pendente_balanca")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemPendenteBalanca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "preco_anterior", nullable = false, precision = 12, scale = 4)
    private BigDecimal precoAnterior;

    @Column(name = "preco_novo", nullable = false, precision = 12, scale = 4)
    private BigDecimal precoNovo;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDENTE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carga_agendada_id")
    private CargaAgendada cargaAgendada;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;
}
