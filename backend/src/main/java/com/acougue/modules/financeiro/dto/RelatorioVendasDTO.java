package com.acougue.modules.financeiro.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RelatorioVendasDTO {
    private BigDecimal totalVendas;
    private int quantidadeVendas;
    private BigDecimal ticketMedio;
    private BigDecimal totalPerdas;
    private List<ProdutoVendaDTO> topProdutos;
}
