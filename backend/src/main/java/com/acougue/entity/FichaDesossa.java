package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ficha_desossa")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FichaDesossa {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_pai_id", nullable = false)
    private Produto produtoPai;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "perda_total_percentual", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal perdaTotalPercentual = BigDecimal.ZERO;

    @Builder.Default
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "fichaDesossa", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequencia ASC")
    @Builder.Default
    private List<FichaDesossaItem> itens = new ArrayList<>();
}
