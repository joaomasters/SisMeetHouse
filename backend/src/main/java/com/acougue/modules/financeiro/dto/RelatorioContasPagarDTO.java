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
public class RelatorioContasPagarDTO {

    /** Saldo devedor total (valor - valorPago) de contas com status ABERTO. */
    private BigDecimal totalAberto;
    /** Parcela já vencida (dataVencimento < hoje). */
    private BigDecimal totalVencido;
    /** Vence nos próximos 7 dias (inclusive hoje). */
    private BigDecimal aVencer7Dias;
    /** Vence nos próximos 30 dias (inclusive hoje). */
    private BigDecimal aVencer30Dias;
    private List<ContaCategoriaDTO> porCategoria;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContaCategoriaDTO {
        private String categoria;
        private BigDecimal total;
        private BigDecimal percentual;
    }
}
