package com.acougue.modules.messaging.events;

import java.math.BigDecimal;

public record AlertaEstoqueEvent(
        Long       produtoId,
        String     nomeProduto,
        BigDecimal estoqueAtual,
        BigDecimal estoqueMinimo,
        
        BigDecimal deficit
) {}
