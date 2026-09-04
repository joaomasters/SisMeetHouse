package com.acougue.modules.balanca;

import com.acougue.exception.InvalidBarcodeException;
import com.acougue.modules.balanca.dto.EanParseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários do parser de EAN-13 de balança.
 *
 * EANs usados nos testes (dígito verificador calculado manualmente pelo
 * algoritmo GS1):
 *   2100000125005 → PLU=10000  valor=R$12,50  (VALOR_TOTAL)
 *   2200000150004 → PLU=20000  peso=1500g=1,5kg  (PESO_GRAMAS)
 *   1234567890128 → EAN padrão (não começa com '2')
 */
@DisplayName("EanBalancaParser")
class EanBalancaParserTest {

    private final EanBalancaParser parser = new EanBalancaParser();

    // ── Formato VALOR_TOTAL ───────────────────────────────────────────────────

    @Test
    @DisplayName("VALOR_TOTAL: valor e peso calculados corretamente")
    void formatoValorTotal_retornaValorEPeso() {
        // PLU=10000, valor=01250 → R$12,50 | preço/kg=R$25,00 → peso=0,500 kg
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

    // ── Formato PESO_GRAMAS ───────────────────────────────────────────────────

    @Test
    @DisplayName("PESO_GRAMAS: peso e valor calculados corretamente")
    void formatoPesoGramas_retornaPesoEValor() {
        // PLU=20000, peso=01500g=1,500 kg | preço/kg=R$30,00 → valor=R$45,00
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

    // ── Validações de formato ─────────────────────────────────────────────────

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
        // EAN correto seria 2100000125005, aqui mudamos o último para 9
        assertThatThrownBy(() -> parser.parse("2100000125009", BigDecimal.TEN))
                .isInstanceOf(InvalidBarcodeException.class)
                .hasMessageContaining("Dígito verificador");
    }

    @Test
    @DisplayName("Lança exceção para EAN que não começa com '2'")
    void lancaExcecao_eanNaoComecaCom2() {
        // EAN padrão válido (não-balança): 1234567890128
        assertThatThrownBy(() ->
            parser.parse("1234567890128", BigDecimal.TEN, EanBalancaParser.FormatoEtiqueta.VALOR_TOTAL)
        ).isInstanceOf(InvalidBarcodeException.class)
         .hasMessageContaining("não é de balança");
    }

    // ── Formato forçado ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Formato forçado PESO_GRAMAS prevalece sobre presença de precoKg")
    void formatoForcado_pesoGramas_prevaleceSobrePrecoKg() {
        // Mesmo com precoKg informado, PESO_GRAMAS é forçado
        EanParseResult result = parser.parse("2200000150004", new BigDecimal("30.00"),
                EanBalancaParser.FormatoEtiqueta.PESO_GRAMAS);
        assertThat(result.getFormato()).isEqualTo(EanBalancaParser.FormatoEtiqueta.PESO_GRAMAS);
        assertThat(result.getPesoKg()).isEqualByComparingTo("1.500");
    }
}
