package com.acougue.modules.financeiro.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FormaPagamentoDTO {
    private String formaPagamento;
    private BigDecimal totalValor;
    private BigDecimal percentual;
}
