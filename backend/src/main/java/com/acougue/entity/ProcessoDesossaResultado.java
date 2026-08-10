package com.acougue.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "processo_desossa_resultado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessoDesossaResultado {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "processo_desossa_id", nullable = false)
    private ProcessoDesossa processoDesossa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_filho_id", nullable = false)
    private Produto produtoFilho;

    @Column(name = "quantidade_prevista", precision = 12, scale = 4)
    private BigDecimal quantidadePrevista;

    @Column(name = "quantidade_real", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantidadeReal;

    @Column(name = "custo_rateado", precision = 12, scale = 4)
    private BigDecimal custoRateado;
}
