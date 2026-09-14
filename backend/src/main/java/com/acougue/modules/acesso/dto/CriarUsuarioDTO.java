package com.acougue.modules.acesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CriarUsuarioDTO {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Login é obrigatório")
    private String login;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 4, message = "Senha deve ter ao menos 4 caracteres")
    private String senha;

    @NotNull(message = "Perfil é obrigatório")
    private Long perfilId;
}