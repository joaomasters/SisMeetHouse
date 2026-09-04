package com.acougue.modules.financeiro.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class VendaDiariaDTO {
    private LocalDate data;
    private int quantidadeVendas;
    private BigDecimal totalVendas;
}
