package com.acougue.modules.pdv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemVendaDTO {
    private Long produtoId;
    private String nomeProduto;
    private String unidadeMedida;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal totalItem;
    private String tipoEntrada; // BARCODE, PESAGEM_DIRETA, MANUAL
    private String ean13Lido;
}
