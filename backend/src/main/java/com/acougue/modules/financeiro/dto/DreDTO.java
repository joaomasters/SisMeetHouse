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
public class DreDTO {

    private String periodo;
    private BigDecimal receitaBruta;
    private BigDecimal cmv;
    private BigDecimal lucroBruto;
    private BigDecimal percentualLucroBruto;
    private BigDecimal custosOperacionais;
    private BigDecimal lucroLiquido;
    private BigDecimal percentualLucroLiquido;

    private List<MargemPorProdutoDTO> margensPorProduto;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MargemPorProdutoDTO {
        private Long produtoId;
        private String nomeProduto;
        private BigDecimal quantidadeVendida;
        private BigDecimal receita;
        private BigDecimal cmv;
        private BigDecimal margem;
        private BigDecimal percentualMargem;
    }
}
