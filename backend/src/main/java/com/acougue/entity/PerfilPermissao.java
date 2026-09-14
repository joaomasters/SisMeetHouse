package com.acougue.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "perfil_permissoes", uniqueConstraints = @UniqueConstraint(columnNames = {"perfil_id", "modulo"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PerfilPermissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false)
    @JsonBackReference
    private Perfil perfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Modulo modulo;

    @Builder.Default
    @Column(name = "pode_ver", nullable = false)
    private Boolean podeVer = false;

    @Builder.Default
    @Column(name = "pode_criar", nullable = false)
    private Boolean podeCriar = false;

    @Builder.Default
    @Column(name = "pode_editar", nullable = false)
    private Boolean podeEditar = false;

    @Builder.Default
    @Column(name = "pode_excluir", nullable = false)
    private Boolean podeExcluir = false;
}