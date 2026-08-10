package com.acougue.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ficha_desossa_itens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FichaDesossaItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_desossa_id", nullable = false)
    private FichaDesossa fichaDesossa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_filho_id", nullable = false)
    private Produto produtoFilho;

    @Column(name = "percentual_rendimento", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualRendimento;

    @Builder.Default
    private Integer sequencia = 0;
}
