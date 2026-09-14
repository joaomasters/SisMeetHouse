package com.acougue.modules.acesso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CriarPerfilDTO {
    @NotBlank(message = "Nome do perfil é obrigatório")
    private String nome;
    private String descricao;
}