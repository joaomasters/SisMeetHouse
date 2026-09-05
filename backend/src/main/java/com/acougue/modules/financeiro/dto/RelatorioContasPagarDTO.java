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

    
    private BigDecimal totalAberto;
    
    private BigDecimal totalVencido;
    
    private BigDecimal aVencer7Dias;
    
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
