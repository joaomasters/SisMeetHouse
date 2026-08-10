package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "caixa")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Caixa {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operador_id")
    private Long operadorId;

    @Column(name = "data_abertura")
    @CreationTimestamp
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @Column(name = "valor_abertura", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorAbertura;

    @Column(name = "valor_fechamento_informado", precision = 12, scale = 2)
    private BigDecimal valorFechamentoInformado;

    @Column(name = "valor_calculado", precision = 12, scale = 2)
    private BigDecimal valorCalculado;

    @Builder.Default
    private String status = "ABERTO"; // ABERTO, FECHADO

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Builder.Default
    @Column(name = "total_sangria", precision = 12, scale = 2)
    private BigDecimal totalSangria = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_suprimento", precision = 12, scale = 2)
    private BigDecimal totalSuprimento = BigDecimal.ZERO;
}
