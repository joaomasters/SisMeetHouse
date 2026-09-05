package com.acougue.modules.messaging.events;

import java.math.BigDecimal;
import java.util.List;

/**
 * Evento publicado APÓS o fechamento de uma venda.
 * Disparado como Spring ApplicationEvent e reenviado ao Kafka pelo VendaEventProducer.
 * Contém o estoque de cada produto após a baixa para que os consumers
 * possam verificar alertas sem consultar o banco novamente.
 */
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
