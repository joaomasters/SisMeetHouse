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

    /** Σ (estoqueAtual × precoCusto) de todos os produtos ativos. */
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
        /** Negativo = quanto falta para atingir o mínimo. */
        private BigDecimal diferenca;
    }
}
