package com.acougue.modules.balanca.dto;

import com.acougue.modules.balanca.EanBalancaParser.FormatoEtiqueta;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EanParseResult {
    int codigoBalanca;
    Double valorTotal;
    double pesoKg;
    FormatoEtiqueta formato;
}
