package com.acougue.modules.financeiro.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProdutoVendaDTO {
    private Long produtoId;
    private String nomeProduto;
    private BigDecimal quantidadeTotal;
    private BigDecimal valorTotal;
}
