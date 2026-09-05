package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "carga_balanca")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CargaBalanca {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "tipo_balanca", nullable = false, length = 20)
    private String tipoBalanca; 

    @Column(name = "codigo_plu", nullable = false)
    private Integer codigoPlu;

    @Column(name = "preco_enviado", nullable = false, precision = 12, scale = 4)
    private BigDecimal precoEnviado;

    @Column(name = "validade_dias")
    @Builder.Default
    private Integer validadeDias = 0;

    @Column(name = "data_envio")
    @CreationTimestamp
    private LocalDateTime dataEnvio;

    @Builder.Default
    private String status = "PENDENTE"; 
}
