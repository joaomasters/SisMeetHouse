package com.acougue.modules.financeiro.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FluxoCaixaDTO {
    private BigDecimal totalRecebimentos;
    private BigDecimal totalPagamentos;
    private BigDecimal saldo;
}
