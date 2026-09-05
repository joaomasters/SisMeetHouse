package com.acougue.modules.financeiro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioEstoqueDTO {

    
    private BigDecimal valorTotalEstoque;
    private int quantidadeProdutos;
    private int produtosAbaixoMinimo;
    private List<AlertaEstoqueDTO> alertas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertaEstoqueDTO {
        private Long produtoId;
        private String nome;
        private String unidadeMedida;
        private BigDecimal estoqueAtual;
        private BigDecimal estoqueMinimo;
        
        private BigDecimal diferenca;
    }
}
