package com.acougue.modules.fiscal.dto;

import java.math.BigDecimal;
import java.util.List;

public record NotaFiscalSaidaDTO(
    Long clienteId,
    String numeroNf,
    String serieNf,
    String naturezaOperacao,
    String observacao,
    List<ItemDTO> itens
) {
    public record ItemDTO(
        Long produtoId,
        String descricao,
        BigDecimal quantidade,
        BigDecimal valorUnitario
    ) {}
}
