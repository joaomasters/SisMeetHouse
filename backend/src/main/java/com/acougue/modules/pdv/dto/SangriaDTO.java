package com.acougue.modules.pdv.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SangriaDTO {
    private String tipo; 
    private BigDecimal valor;
    private String motivo;
    private Long operadorId;
}
