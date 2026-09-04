package com.acougue.modules.financeiro.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RelatorioVendasDTO {
    // ── Totais gerais ──────────────────────────────────────────────────────────
    private BigDecimal totalVendas;
    private int quantidadeVendas;
    private BigDecimal ticketMedio;

    // ── CMV e margem ──────────────────────────────────────────────────────────
    private BigDecimal totalCMV;
    private BigDecimal margemBruta;
    /** Percentual de margem bruta sobre a receita (0-100). */
    private BigDecimal percentualMargem;

    // ── Perdas ─────────────────────────────────────────────────────────────────
    private BigDecimal totalPerdas;

    // ── Ranking de produtos ────────────────────────────────────────────────────
    private List<ProdutoVendaDTO> topProdutos;

    // ── Breakdown por forma de pagamento ──────────────────────────────────────
    private List<FormaPagamentoDTO> porFormaPagamento;

    // ── Evolução diária ────────────────────────────────────────────────────────
    private List<VendaDiariaDTO> vendasPorDia;
}
