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
public class RelatorioPerdasDTO {

    private BigDecimal totalCusto;
    private int quantidadeRegistros;
    /** Custo total de perdas dividido pelo total de vendas no período (impacto no resultado). */
    private BigDecimal percentualImpactoVendas;
    private List<PerdaMotivoDTO> porMotivo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerdaMotivoDTO {
        private String motivo;
        private int quantidade;
        private BigDecimal custo;
        /** Percentual desse motivo sobre o custo total de perdas. */
        private BigDecimal percentual;
    }
}
