package com.acougue.modules.financeiro.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContasPagarDTO {
    private String descricao;
    private String fornecedor;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private String categoria;
    private String observacao;
}
