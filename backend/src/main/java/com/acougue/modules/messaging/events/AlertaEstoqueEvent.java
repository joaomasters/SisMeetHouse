package com.acougue.modules.messaging.events;

import java.math.BigDecimal;

/**
 * Evento publicado quando o estoque de um produto cai abaixo do mínimo
 * após uma venda. Extensível para notificações (email, push, dashboard).
 */
public record AlertaEstoqueEvent(
        Long       produtoId,
        String     nomeProduto,
        BigDecimal estoqueAtual,
        BigDecimal estoqueMinimo,
        /** Quanto falta para atingir o mínimo (negativo = abaixo do mínimo). */
        BigDecimal deficit
) {}
