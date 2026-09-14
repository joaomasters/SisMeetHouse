package com.acougue.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perfis")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Perfil {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do perfil é obrigatório")
    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    // Perfis protegidos (ex: SUPER_ADMIN) não podem ser excluídos nem renomeados
    @Builder.Default
    @Column(nullable = false)
    private Boolean protegido = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<PerfilPermissao> permissoes = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // Perfil SUPER_ADMIN tem acesso irrestrito, independentemente das linhas de permissão gravadas
    public boolean isSuperAdmin() {
        return SUPER_ADMIN.equals(nome);
    }
}