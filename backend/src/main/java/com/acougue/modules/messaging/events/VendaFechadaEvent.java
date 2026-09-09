package com.acougue.modules.messaging.events;

import java.math.BigDecimal;
import java.util.List;

public record VendaFechadaEvent(
        Long vendaId,
        Long operadorId,
        BigDecimal total,
        List<ItemEvent> itens
) {
    public record ItemEvent(
            Long      produtoId,
            String    nomeProduto,
            BigDecimal quantidade,
            BigDecimal estoqueAposVenda,
            BigDecimal estoqueMinimo
    ) {}
}
