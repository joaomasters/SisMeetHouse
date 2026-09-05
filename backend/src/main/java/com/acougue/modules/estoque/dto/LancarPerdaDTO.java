package com.acougue.modules.estoque.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LancarPerdaDTO {
    private Long produtoId;
    private BigDecimal quantidade;
    private String motivo; 
    private String observacao;
    private Long usuarioId;
}
