package com.acougue.modules.balanca.dto;

import com.acougue.modules.balanca.EanBalancaParser.FormatoEtiqueta;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class EanParseResult {
    int codigoBalanca;
    BigDecimal valorTotal;
    BigDecimal pesoKg;
    FormatoEtiqueta formato;
}
