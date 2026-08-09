package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "cpf_cnpj", length = 18)
    private String cpfCnpj;

    @Column(name = "tipo_pessoa", length = 5)
    @Builder.Default
    private String tipoPessoa = "PF"; // PF, PJ

    @Column(length = 20)
    private String telefone;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String endereco;

    @Column(name = "tipo_cliente", length = 20)
    @Builder.Default
    private String tipoCliente = "VAREJO"; // VAREJO, ATACADO, RESTAURANTE, CONVENIADO

    @Column(name = "limite_credito", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Builder.Default
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
