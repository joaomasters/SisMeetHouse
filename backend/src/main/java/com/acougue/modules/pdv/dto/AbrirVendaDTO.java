package com.acougue.modules.pdv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbrirVendaDTO {
    private Long operadorId;
    private Long caixaId;
    private Long clienteId;
}
