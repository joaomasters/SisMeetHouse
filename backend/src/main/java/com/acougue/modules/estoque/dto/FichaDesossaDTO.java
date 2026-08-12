package com.acougue.modules.estoque.dto;

import java.math.BigDecimal;
import java.util.List;

public record FichaDesossaDTO(
    String nome,
    String descricao,
    Long produtoPaiId,
    List<ItemDTO> itens
) {
    public record ItemDTO(
        Long    produtoFilhoId,
        BigDecimal percentualRendimento,
        Integer sequencia
    ) {}
}
