package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "carga_agendada")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CargaAgendada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_balanca", nullable = false, length = 30)
    private String tipoBalanca;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDENTE";

    @Column(name = "produtos_count", nullable = false)
    @Builder.Default
    private Integer produtosCount = 0;

    @Column(name = "conteudo_carga", nullable = false, columnDefinition = "TEXT")
    private String conteudoCarga;

    @Column(name = "ip_balanca", length = 50)
    private String ipBalanca;

    @Column(name = "porta_balanca", nullable = false)
    @Builder.Default
    private Integer portaBalanca = 8000;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "aplicado_em")
    private LocalDateTime aplicadoEm;

    @Column(name = "erro_mensagem", columnDefinition = "TEXT")
    private String erroMensagem;
}
