package com.acougue.modules.acesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtualizarUsuarioDTO {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull(message = "Perfil é obrigatório")
    private Long perfilId;
}