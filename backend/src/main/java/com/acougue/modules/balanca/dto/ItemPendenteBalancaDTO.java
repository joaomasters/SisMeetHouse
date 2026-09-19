package com.acougue.modules.balanca.dto;

import com.acougue.entity.ItemPendenteBalanca;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class ItemPendenteBalancaDTO {
    Long id;
    Long produtoId;
    String produtoNome;
    Integer codigoBalanca;
    BigDecimal precoAnterior;
    BigDecimal precoNovo;
    String status;
    LocalDateTime criadoEm;

    public static ItemPendenteBalancaDTO from(ItemPendenteBalanca item) {
        return ItemPendenteBalancaDTO.builder()
                .id(item.getId())
                .produtoId(item.getProduto().getId())
                .produtoNome(item.getProduto().getNome())
                .codigoBalanca(item.getProduto().getCodigoBalanca())
                .precoAnterior(item.getPrecoAnterior())
                .precoNovo(item.getPrecoNovo())
                .status(item.getStatus())
                .criadoEm(item.getCriadoEm())
                .build();
    }
}
