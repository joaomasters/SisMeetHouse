package com.acougue.modules.acesso.dto;

import lombok.Data;

import java.util.List;

@Data
public class AtualizarPermissoesDTO {
    private List<PermissaoItemDTO> permissoes;
}