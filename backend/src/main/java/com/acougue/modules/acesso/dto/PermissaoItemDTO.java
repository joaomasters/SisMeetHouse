package com.acougue.modules.acesso.dto;

import com.acougue.entity.Modulo;
import lombok.Data;

@Data
public class PermissaoItemDTO {
    private Modulo modulo;
    private boolean ver;
    private boolean criar;
    private boolean editar;
    private boolean excluir;
}