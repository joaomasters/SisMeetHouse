package com.acougue.modules.acesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TrocarSenhaDTO {
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 4, message = "Senha deve ter ao menos 4 caracteres")
    private String novaSenha;
}