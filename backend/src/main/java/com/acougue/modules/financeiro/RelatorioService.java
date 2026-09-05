package com.acougue.modules.financeiro;

import com.acougue.entity.ContasPagar;
import com.acougue.entity.ItensVenda;
import com.acougue.entity.PerdasEstoque;
import com.acougue.entity.Produto;
import com.acougue.entity.Venda;
import com.acougue.modules.financeiro.dto.*;
import com.acougue.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final VendaRepository          vendaRepo;
    private final ItensVendaRepository     itensRepo;
    private final PerdasEstoqueRepository  perdasRepo;
    private final ContasPagarRepository    contasPagarRepo;
    private final PagamentoVendaRepository pagRepo;
    private final ProdutoRepository        produtoRepo;

    // ── 1. Relatório de Vendas (melhorado) ────────────────────────────────────

    /**
     * Relatório consolidado de vendas com CMV, margem bruta, breakdown de
     * formas de pagamento e evolução diária.
     */
    public RelatorioVendasDTO relatorioVendas(LocalDateTime inicio, LocalDateTime fim) {
        List<Venda> vendas = vendaRepo.findByPeriodo(inicio, fim).stream()
                .filter(v -> "FECHADA".equals(v.getStatus()))
                .collect(Collectors.toList());

        BigDecimal totalVendas = vendas.stream()
                .map(Venda::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ticketMedio = vendas.isEmpty() ? BigDecimal.ZERO
                : totalVendas.divide(BigDecimal.valueOf(vendas.size()), 2, RoundingMode.HALF_UP);

        // CMV via query agregada (sem N+1)
        BigDecimal totalCMV = itensRepo.somarCMVPeriodo(inicio, fim);
        BigDecimal margemBruta = totalVendas.subtract(totalCMV).setScale(2, RoundingMode.HALF_UP);
        BigDecimal percentualMargem = totalVendas.compareTo(BigDecimal.ZERO) > 0
                ? margemBruta.divide(totalVendas, 4, RoundingMode.HALF_UP)
                             .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Top 10 produtos por receita
        List<ProdutoVendaDTO> topProdutos = construirTopProdutos(vendas);

        // Perdas do período
        BigDecimal totalPerdas = perdasRepo.findByPeriodo(inicio, fim).stream()
                .map(p -> p.getCustoTotal() != null ? p.getCustoTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Formas de pagamento
        List<FormaPagamentoDTO> porForma = construirBreakdownFormas(inicio, fim);

        // Evolução diária
        List<VendaDiariaDTO> porDia = construirEvolucaoDiaria(vendas);

        return RelatorioVendasDTO.builder()
                .totalVendas(totalVendas)
                .quantidadeVendas(vendas.size())
                .ticketMedio(ticketMedio)
                .totalCMV(totalCMV)
                .margemBruta(margemBruta)
                .percentualMargem(percentualMargem)
                .totalPerdas(totalPerdas)
                .topProdutos(topProdutos)
                .porFormaPagamento(porForma)
                .vendasPorDia(porDia)
                .build();
    }

    // ── 2. Relatório de Perdas ────────────────────────────────────────────────

    /**
     * Breakdown de perdas de estoque por motivo (VENCIMENTO, AVARIA, FURTO, …).
     * Inclui percentual de impacto sobre a receita do período.
     */
    public RelatorioPerdasDTO relatorioPerdas(LocalDateTime inicio, LocalDateTime fim) {
        List<PerdasEstoque> perdas = perdasRepo.findByPeriodo(inicio, fim);

        BigDecimal totalCusto = perdas.stream()
                .map(p -> p.getCustoTotal() != null ? p.getCustoTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receitaPeriodo = vendaRepo.somarTotalPeriodo(inicio, fim);
        BigDecimal impacto = receitaPeriodo.compareTo(BigDecimal.ZERO) > 0
                ? totalCusto.divide(receitaPeriodo, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Agrupa por motivo
        Map<String, List<PerdasEstoque>> porMotivo = perdas.stream()
                .collect(Collectors.groupingBy(PerdasEstoque::getMotivo));

        List<RelatorioPerdasDTO.PerdaMotivoDTO> motivoDTOs = porMotivo.entrySet().stream()
                .map(e -> {
                    BigDecimal custo = e.getValue().stream()
                            .map(p -> p.getCustoTotal() != null ? p.getCustoTotal() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal pct = totalCusto.compareTo(BigDecimal.ZERO) > 0
                            ? custo.divide(totalCusto, 4, RoundingMode.HALF_UP)
                                   .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return RelatorioPerdasDTO.PerdaMotivoDTO.builder()
                            .motivo(e.getKey())
                            .quantidade(e.getValue().size())
                            .custo(custo)
                            .percentual(pct)
                            .build();
                })
                .sorted(Comparator.comparing(RelatorioPerdasDTO.PerdaMotivoDTO::getCusto).reversed())
                .collect(Collectors.toList());

        return RelatorioPerdasDTO.builder()
                .totalCusto(totalCusto)
                .quantidadeRegistros(perdas.size())
                .percentualImpactoVendas(impacto)
                .porMotivo(motivoDTOs)
                .build();
    }

    // ── 3. Relatório de Estoque Atual ─────────────────────────────────────────

    /**
     * Snapshot do estoque: valor total em câmara, produtos abaixo do mínimo
     * e lista de alertas ordenada pelo maior deficit.
     */
    public RelatorioEstoqueDTO relatorioEstoque() {
        List<Produto> todos = produtoRepo.findAllByAtivoTrue();
        List<Produto> abaixoMinimo = produtoRepo.findEstoqueAbaixoMinimo();

        BigDecimal valorTotal = todos.stream()
                .map(p -> {
                    BigDecimal custo = p.getPrecoCusto() != null ? p.getPrecoCusto() : BigDecimal.ZERO;
                    return p.getEstoqueAtual().multiply(custo).setScale(2, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        List<RelatorioEstoqueDTO.AlertaEstoqueDTO> alertas = abaixoMinimo.stream()
                .map(p -> RelatorioEstoqueDTO.AlertaEstoqueDTO.builder()
                        .produtoId(p.getId())
                        .nome(p.getNome())
                        .unidadeMedida(p.getUnidadeMedida())
                        .estoqueAtual(p.getEstoqueAtual())
                        .estoqueMinimo(p.getEstoqueMinimo())
                        .diferenca(p.getEstoqueAtual().subtract(p.getEstoqueMinimo()))
                        .build())
                .sorted(Comparator.comparing(RelatorioEstoqueDTO.AlertaEstoqueDTO::getDiferenca))
                .collect(Collectors.toList());

        return RelatorioEstoqueDTO.builder()
                .valorTotalEstoque(valorTotal)
                .quantidadeProdutos(todos.size())
                .produtosAbaixoMinimo(abaixoMinimo.size())
                .alertas(alertas)
                .build();
    }

    // ── 4. Relatório de Contas a Pagar (aging) ────────────────────────────────

    /**
     * Aging de contas a pagar: vencidas, a vencer em 7 e 30 dias, total aberto
     * e breakdown por categoria de despesa. Recebe 'hoje' como parâmetro para
     * facilitar testes unitários.
     */
    public RelatorioContasPagarDTO relatorioContasPagar(LocalDate hoje) {
        LocalDate em7  = hoje.plusDays(7);
        LocalDate em30 = hoje.plusDays(30);

        List<ContasPagar> abertas = contasPagarRepo.findByStatusOrderByDataVencimentoAsc("ABERTO");

        BigDecimal totalAberto = abertas.stream()
                .map(this::saldoDevedor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVencido = abertas.stream()
                .filter(c -> c.getDataVencimento().isBefore(hoje))
                .map(this::saldoDevedor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal aVencer7 = abertas.stream()
                .filter(c -> !c.getDataVencimento().isBefore(hoje)
                          && !c.getDataVencimento().isAfter(em7))
                .map(this::saldoDevedor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal aVencer30 = abertas.stream()
                .filter(c -> !c.getDataVencimento().isBefore(hoje)
                          && !c.getDataVencimento().isAfter(em30))
                .map(this::saldoDevedor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Breakdown por categoria
        Map<String, List<ContasPagar>> porCat = abertas.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCategoria() != null ? c.getCategoria() : "Sem categoria"));

        List<RelatorioContasPagarDTO.ContaCategoriaDTO> catDTOs = porCat.entrySet().stream()
                .map(e -> {
                    BigDecimal total = e.getValue().stream()
                            .map(this::saldoDevedor)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal pct = totalAberto.compareTo(BigDecimal.ZERO) > 0
                            ? total.divide(totalAberto, 4, RoundingMode.HALF_UP)
                                   .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return RelatorioContasPagarDTO.ContaCategoriaDTO.builder()
                            .categoria(e.getKey())
                            .total(total)
                            .percentual(pct)
                            .build();
                })
                .sorted(Comparator.comparing(RelatorioContasPagarDTO.ContaCategoriaDTO::getTotal).reversed())
                .collect(Collectors.toList());

        return RelatorioContasPagarDTO.builder()
                .totalAberto(totalAberto)
                .totalVencido(totalVencido)
                .aVencer7Dias(aVencer7)
                .aVencer30Dias(aVencer30)
                .porCategoria(catDTOs)
                .build();
    }

    // ── 5. Fluxo de Caixa (melhorado com saldo projetado) ─────────────────────

    public FluxoCaixaDTO fluxoCaixa(LocalDate inicio, LocalDate fim) {
        LocalDateTime dtInicio = inicio.atStartOfDay();
        LocalDateTime dtFim    = fim.atTime(LocalTime.MAX);

        BigDecimal totalRecebimentos = vendaRepo.findByPeriodo(dtInicio, dtFim).stream()
                .filter(v -> "FECHADA".equals(v.getStatus()))
                .map(Venda::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPagamentos = contasPagarRepo
                .findByDataVencimentoBetweenOrderByDataVencimentoAsc(inicio, fim).stream()
                .filter(c -> "PAGO".equals(c.getStatus()) || "PARCIAL".equals(c.getStatus()))
                .map(ContasPagar::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FluxoCaixaDTO.builder()
                .totalRecebimentos(totalRecebimentos)
                .totalPagamentos(totalPagamentos)
                .saldo(totalRecebimentos.subtract(totalPagamentos))
                .build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private BigDecimal saldoDevedor(ContasPagar c) {
        BigDecimal pago = c.getValorPago() != null ? c.getValorPago() : BigDecimal.ZERO;
        return c.getValor().subtract(pago).max(BigDecimal.ZERO);
    }

    private List<ProdutoVendaDTO> construirTopProdutos(List<Venda> vendas) {
        Map<Long, ProdutoVendaDTO> map = new LinkedHashMap<>();
        for (Venda venda : vendas) {
            for (ItensVenda item : itensRepo.findByVendaId(venda.getId())) {
                Long pid = item.getProduto().getId();
                map.compute(pid, (k, ex) -> {
                    if (ex == null) return ProdutoVendaDTO.builder()
                            .produtoId(pid)
                            .nomeProduto(item.getProduto().getNome())
                            .quantidadeTotal(item.getQuantidade())
                            .valorTotal(item.getTotalItem())
                            .build();
                    ex.setQuantidadeTotal(ex.getQuantidadeTotal().add(item.getQuantidade()));
                    ex.setValorTotal(ex.getValorTotal().add(item.getTotalItem()));
                    return ex;
                });
            }
        }
        return map.values().stream()
                .sorted(Comparator.comparing(ProdutoVendaDTO::getValorTotal).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<FormaPagamentoDTO> construirBreakdownFormas(LocalDateTime inicio, LocalDateTime fim) {
        List<Object[]> raw = pagRepo.totaisPorFormaPagamento(inicio, fim);
        BigDecimal totalGeral = raw.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return raw.stream()
                .map(r -> {
                    BigDecimal val = (BigDecimal) r[1];
                    BigDecimal pct = totalGeral.compareTo(BigDecimal.ZERO) > 0
                            ? val.divide(totalGeral, 4, RoundingMode.HALF_UP)
                                 .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return FormaPagamentoDTO.builder()
                            .formaPagamento((String) r[0])
                            .totalValor(val)
                            .percentual(pct)
                            .build();
                })
                .sorted(Comparator.comparing(FormaPagamentoDTO::getTotalValor).reversed())
                .collect(Collectors.toList());
    }

    private List<VendaDiariaDTO> construirEvolucaoDiaria(List<Venda> vendas) {
        Map<java.time.LocalDate, List<Venda>> porDia = vendas.stream()
                .collect(Collectors.groupingBy(v -> v.getDataVenda().toLocalDate()));

        return porDia.entrySet().stream()
                .map(e -> {
                    BigDecimal totalDia = e.getValue().stream()
                            .map(Venda::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, RoundingMode.HALF_UP);
                    return VendaDiariaDTO.builder()
                            .data(e.getKey())
                            .quantidadeVendas(e.getValue().size())
                            .totalVendas(totalDia)
                            .build();
                })
                .sorted(Comparator.comparing(VendaDiariaDTO::getData))
                .collect(Collectors.toList());
    }
}
