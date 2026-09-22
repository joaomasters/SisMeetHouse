package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Registro de uma ação de CRIAR/EDITAR/EXCLUIR feita por algum usuário,
 * gravado automaticamente por {@code AuditoriaAspect}. Não guarda relação
 * JPA com {@code Usuario} de propósito — o log precisa sobreviver e manter
 * o nome de quem fez a ação mesmo que esse usuário seja excluído ou
 * renomeado depois.
 */
@Entity
@Table(name = "log_auditoria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "nome_usuario", nullable = false, length = 150)
    private String nomeUsuario;

    @Column(name = "perfil_usuario", length = 50)
    private String perfilUsuario;

    @Column(nullable = false, length = 40)
    private String modulo;

    @Column(nullable = false, length = 20)
    private String acao;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}