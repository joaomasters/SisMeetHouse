package com.acougue.modules.balanca;

import com.acougue.exception.InvalidBarcodeException;
import com.acougue.modules.balanca.dto.EanParseResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Decodifica EAN-13 ponderado (prefixo "2") emitido por balanças.
 *
 * Formato A — Valor Total:  2 [PPPPP] [VVVVV] [D]
 *   P = 5 dígitos do PLU do produto
 *   V = 5 dígitos do valor em centavos  (ex: 01250 = R$12,50)
 *
 * Formato B — Peso em Gramas: 2 [PPPPP] [GGGGG] [D]
 *   G = 5 dígitos do peso em gramas     (ex: 01500 = 1.500g = 1,5 kg)
 *
 * PRIORIDADE: sempre Formato A quando o produto tem preço/kg cadastrado,
 * pois evita divergência de centavos por arredondamento no caixa.
 */
@Component
public class EanBalancaParser {

    public enum FormatoEtiqueta { VALOR_TOTAL, PESO_GRAMAS }

    public EanParseResult parse(String ean13, BigDecimal precoVendaKg) {
        return parse(ean13, precoVendaKg, null);
    }

    public EanParseResult parse(String ean13, BigDecimal precoVendaKg, FormatoEtiqueta formatoForcado) {
        validarFormato(ean13);
        validarDigitoVerificador(ean13);

        if (!ean13.startsWith("2")) {
            throw new InvalidBarcodeException("EAN não é de balança (deve iniciar com '2'): " + ean13);
        }

        int codigoBalanca = Integer.parseInt(ean13.substring(1, 6));
        int campoNumerico = Integer.parseInt(ean13.substring(6, 11));

        FormatoEtiqueta formato = resolverFormato(formatoForcado, precoVendaKg);

        return switch (formato) {
            case VALOR_TOTAL -> parseByValor(codigoBalanca, campoNumerico, precoVendaKg);
            case PESO_GRAMAS -> parseByPeso(codigoBalanca, campoNumerico, precoVendaKg);
        };
    }

    // --- Formato A: lê valor, calcula peso ---
    private EanParseResult parseByValor(int codigoBalanca, int campoNumerico, BigDecimal precoKg) {
        if (precoKg == null || precoKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBarcodeException(
                "Preço/kg inválido ou não cadastrado para calcular peso pelo valor.");
        }
        BigDecimal valorTotal = BigDecimal.valueOf(campoNumerico).divide(
            BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal pesoKg = valorTotal.divide(precoKg, 3, RoundingMode.HALF_UP);

        return EanParseResult.builder()
                .codigoBalanca(codigoBalanca)
                .valorTotal(valorTotal)
                .pesoKg(pesoKg)
                .formato(FormatoEtiqueta.VALOR_TOTAL)
                .build();
    }

    // --- Formato B: lê peso, calcula valor ---
    private EanParseResult parseByPeso(int codigoBalanca, int campoNumerico, BigDecimal precoKg) {
        BigDecimal pesoKg = BigDecimal.valueOf(campoNumerico).divide(
            BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

        BigDecimal valorTotal = (precoKg != null && precoKg.compareTo(BigDecimal.ZERO) > 0)
                ? pesoKg.multiply(precoKg).setScale(2, RoundingMode.HALF_UP)
                : null;

        return EanParseResult.builder()
                .codigoBalanca(codigoBalanca)
                .valorTotal(valorTotal)
                .pesoKg(pesoKg)
                .formato(FormatoEtiqueta.PESO_GRAMAS)
                .build();
    }

    private FormatoEtiqueta resolverFormato(FormatoEtiqueta forcado, BigDecimal precoKg) {
        if (forcado != null) return forcado;
        return (precoKg != null && precoKg.compareTo(BigDecimal.ZERO) > 0)
                ? FormatoEtiqueta.VALOR_TOTAL
                : FormatoEtiqueta.PESO_GRAMAS;
    }

    private void validarFormato(String ean13) {
        if (ean13 == null || !ean13.matches("\\d{13}")) {
            throw new InvalidBarcodeException(
                "EAN inválido — deve conter exatamente 13 dígitos numéricos. Recebido: " + ean13);
        }
    }

    /** Algoritmo padrão GS1 para cálculo do dígito verificador EAN-13 */
    private void validarDigitoVerificador(String ean13) {
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            int digito = ean13.charAt(i) - '0';
            soma += (i % 2 == 0) ? digito : digito * 3;
        }
        int calculado = (10 - (soma % 10)) % 10;
        int informado  = ean13.charAt(12) - '0';

        if (calculado != informado) {
            throw new InvalidBarcodeException(String.format(
                "Dígito verificador inválido. Calculado=%d, Informado=%d | EAN: %s",
                calculado, informado, ean13));
        }
    }
}
