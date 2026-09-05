package com.acougue.modules.balanca;

import com.acougue.exception.InvalidBarcodeException;
import com.acougue.modules.balanca.dto.EanParseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EanBalancaParser")
class EanBalancaParserTest {

    private final EanBalancaParser parser = new EanBalancaParser();

    

    @Test
    @DisplayName("VALOR_TOTAL: valor e peso calculados corretamente")
    void formatoValorTotal_retornaValorEPeso() {
        
        EanParseResult result = parser.parse("2100000125005", new BigDecimal("25.00"));

        assertThat(result.getCodigoBalanca()).isEqualTo(10000);
        assertThat(result.getValorTotal()).isEqualByComparingTo("12.50");
        assertThat(result.getPesoKg()).isEqualByComparingTo("0.500");
        assertThat(result.getFormato()).isEqualTo(EanBalancaParser.FormatoEtiqueta.VALOR_TOTAL);
    }

    @Test
    @DisplayName("VALOR_TOTAL: escolhido automaticamente quando precoKg está informado")
    void formatoValorTotal_escolhidoAutomaticamente_quandoPrecoKgPresente() {
        EanParseResult result = parser.parse("2100000125005", new BigDecimal("50.00"));
        assertThat(result.getFormato()).isEqualTo(EanBalancaParser.FormatoEtiqueta.VALOR_TOTAL);
    }

    @Test
    @DisplayName("VALOR_TOTAL: lança exceção quando precoKg é nulo")
    void formatoValorTotal_lancaExcecao_quandoPrecoKgNulo() {
        assertThatThrownBy(() ->
            parser.parse("2100000125005", null, EanBalancaParser.FormatoEtiqueta.VALOR_TOTAL)
        ).isInstanceOf(InvalidBarcodeException.class)
         .hasMessageContaining("Preço/kg inválido");
    }

    @Test
    @DisplayName("VALOR_TOTAL: lança exceção quando precoKg é zero")
    void formatoValorTotal_lancaExcecao_quandoPrecoKgZero() {
        assertThatThrownBy(() ->
            parser.parse("2100000125005", BigDecimal.ZERO, EanBalancaParser.FormatoEtiqueta.VALOR_TOTAL)
        ).isInstanceOf(InvalidBarcodeException.class);
    }

    

    @Test
    @DisplayName("PESO_GRAMAS: peso e valor calculados corretamente")
    void formatoPesoGramas_retornaPesoEValor() {
        
        EanParseResult result = parser.parse("2200000150004", new BigDecimal("30.00"),
                EanBalancaParser.FormatoEtiqueta.PESO_GRAMAS);

        assertThat(result.getCodigoBalanca()).isEqualTo(20000);
        assertThat(result.getPesoKg()).isEqualByComparingTo("1.500");
        assertThat(result.getValorTotal()).isEqualByComparingTo("45.00");
        assertThat(result.getFormato()).isEqualTo(EanBalancaParser.FormatoEtiqueta.PESO_GRAMAS);
    }

    @Test
    @DisplayName("PESO_GRAMAS: valorTotal é nulo quando precoKg não informado")
    void formatoPesoGramas_valorTotalNulo_semPrecoKg() {
        EanParseResult result = parser.parse("2200000150004", null,
                EanBalancaParser.FormatoEtiqueta.PESO_GRAMAS);

        assertThat(result.getPesoKg()).isEqualByComparingTo("1.500");
        assertThat(result.getValorTotal()).isNull();
    }

    @Test
    @DisplayName("PESO_GRAMAS: escolhido automaticamente quando precoKg é nulo")
    void formatoPesoGramas_escolhidoAutomaticamente_semPrecoKg() {
        EanParseResult result = parser.parse("2200000150004", null);
        assertThat(result.getFormato()).isEqualTo(EanBalancaParser.FormatoEtiqueta.PESO_GRAMAS);
    }

    

    @Test
    @DisplayName("Lança exceção para EAN com menos de 13 dígitos")
    void lancaExcecao_eanMenorQue13Digitos() {
        assertThatThrownBy(() -> parser.parse("210000012500", BigDecimal.TEN))
                .isInstanceOf(InvalidBarcodeException.class)
                .hasMessageContaining("13 dígitos");
    }

    @Test
    @DisplayName("Lança exceção para EAN com mais de 13 dígitos")
    void lancaExcecao_eanMaiorQue13Digitos() {
        assertThatThrownBy(() -> parser.parse("21000001250055", BigDecimal.TEN))
                .isInstanceOf(InvalidBarcodeException.class)
                .hasMessageContaining("13 dígitos");
    }

    @Test
    @DisplayName("Lança exceção para EAN com letras")
    void lancaExcecao_eanComLetras() {
        assertThatThrownBy(() -> parser.parse("21000001250AB", BigDecimal.TEN))
                .isInstanceOf(InvalidBarcodeException.class);
    }

    @Test
    @DisplayName("Lança exceção para EAN nulo")
    void lancaExcecao_eanNulo() {
        assertThatThrownBy(() -> parser.parse(null, BigDecimal.TEN))
                .isInstanceOf(InvalidBarcodeException.class);
    }

    @Test
    @DisplayName("Lança exceção para dígito verificador incorreto")
    void lancaExcecao_digitoVerificadorIncorreto() {
        
        assertThatThrownBy(() -> parser.parse("2100000125009", BigDecimal.TEN))
                .isInstanceOf(InvalidBarcodeException.class)
                .hasMessageContaining("Dígito verificador");
    }

    @Test
    @DisplayName("Lança exceção para EAN que não começa com '2'")
    void lancaExcecao_eanNaoComecaCom2() {
        
        assertThatThrownBy(() ->
            parser.parse("1234567890128", BigDecimal.TEN, EanBalancaParser.FormatoEtiqueta.VALOR_TOTAL)
        ).isInstanceOf(InvalidBarcodeException.class)
         .hasMessageContaining("não é de balança");
    }

    

    @Test
    @DisplayName("Formato forçado PESO_GRAMAS prevalece sobre presença de precoKg")
    void formatoForcado_pesoGramas_prevaleceSobrePrecoKg() {
        
        EanParseResult result = parser.parse("2200000150004", new BigDecimal("30.00"),
                EanBalancaParser.FormatoEtiqueta.PESO_GRAMAS);
        assertThat(result.getFormato()).isEqualTo(EanBalancaParser.FormatoEtiqueta.PESO_GRAMAS);
        assertThat(result.getPesoKg()).isEqualByComparingTo("1.500");
    }
}
