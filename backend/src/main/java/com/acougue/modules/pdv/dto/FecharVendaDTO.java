package com.acougue.modules.pdv.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FecharVendaDTO {

    @NotNull
    private Long vendaId;

    @NotEmpty
    @Valid
    private List<PagamentoDTO> pagamentos;
}
