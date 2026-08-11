package com.acougue.modules.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutarDesossaDTO {

    @NotNull
    private Long fichaDesossaId;

    @NotNull
    @DecimalMin("0.001")
    private BigDecimal quantidadeKgEntrada;

    private BigDecimal custoPorKg;

    // Mapa produtoFilhoId → quantidade real obtida na pesagem (opcional)
    private Map<Long, BigDecimal> quantidadesReais;

    private Long usuarioId;
    private String observacao;
    private Long recebimentoId;
}
