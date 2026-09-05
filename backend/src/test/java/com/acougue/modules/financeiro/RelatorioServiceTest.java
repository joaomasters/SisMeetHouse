package com.acougue.modules.financeiro;

import com.acougue.entity.*;
import com.acougue.modules.financeiro.dto.*;
import com.acougue.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelatorioService")
class RelatorioServiceTest {

    @Mock VendaRepository          vendaRepo;
    @Mock ItensVendaRepository     itensRepo;
    @Mock PerdasEstoqueRepository  perdasRepo;
    @Mock ContasPagarRepository    contasPagarRepo;
    @Mock PagamentoVendaRepository pagRepo;
    @Mock ProdutoRepository        produtoRepo;

    @InjectMocks RelatorioService service;

    private static final LocalDateTime INI = LocalDate.of(2026, 9, 1).atStartOfDay();
    private static final LocalDateTime FIM = LocalDate.of(2026, 9, 30).atTime(LocalTime.MAX);

    // ── fixtures ──────────────────────────────────────────────────────────────

    private Produto picanha;
    private Produto contrafile;

    @BeforeEach
    void setUp() {
        picanha = Produto.builder().id(1L).nome("Picanha")
                .precoVenda(new BigDecimal("89.90")).precoCusto(new BigDecimal("45.00"))
                .estoqueAtual(new BigDecimal("10.000")).estoqueMinimo(new BigDecimal("5.000"))
                .unidadeMedida("KG").build();

        contrafile = Produto.builder().id(2L).nome("Contrafilé")
                .precoVenda(new BigDecimal("59.90")).precoCusto(new BigDecimal("30.00"))
                .estoqueAtual(new BigDecimal("3.000")).estoqueMinimo(new BigDecimal("8.000"))
                .unidadeMedida("KG").build();
    }

    // ── relatorioVendas ───────────────────────────────────────────────────────

    @Test
    @DisplayName("relatorioVendas: retorna zeros quando não há vendas")
    void relatorioVendas_semVendas_retornaZeros() {
        when(vendaRepo.findByPeriodo(INI, FIM)).thenReturn(List.of());
        when(itensRepo.somarCMVPeriodo(INI, FIM)).thenReturn(BigDecimal.ZERO);
        when(perdasRepo.findByPeriodo(INI, FIM)).thenReturn(List.of());
        when(pagRepo.totaisPorFormaPagamento(INI, FIM)).thenReturn(List.of());

        RelatorioVendasDTO dto = service.relatorioVendas(INI, FIM);

        assertThat(dto.getTotalVendas()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.getQuantidadeVendas()).isZero();
        assertThat(dto.getTicketMedio()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.getPercentualMargem()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.getTopProdutos()).isEmpty();
        assertThat(dto.getVendasPorDia()).isEmpty();
    }

    @Test
    @DisplayName("relatorioVendas: calcula ticket médio corretamente")
    void relatorioVendas_calculaTicketMedio() {
        Venda v1 = vendaFechada(1L, "2026-09-10", new BigDecimal("100.00"));
        Venda v2 = vendaFechada(2L, "2026-09-10", new BigDecimal("200.00"));

        when(vendaRepo.findByPeriodo(INI, FIM)).thenReturn(List.of(v1, v2));
        when(itensRepo.somarCMVPeriodo(INI, FIM)).thenReturn(new BigDecimal("120.00"));
        when(perdasRepo.findByPeriodo(INI, FIM)).thenReturn(List.of());
        when(pagRepo.totaisPorFormaPagamento(INI, FIM)).thenReturn(List.of());
        when(itensRepo.findByVendaId(anyLong())).thenReturn(List.of());

        RelatorioVendasDTO dto = service.relatorioVendas(INI, FIM);

        assertThat(dto.getTotalVendas()).isEqualByComparingTo("300.00");
        assertThat(dto.getQuantidadeVendas()).isEqualTo(2);
        assertThat(dto.getTicketMedio()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("relatorioVendas: calcula margem bruta e percentual")
    void relatorioVendas_calculaMargemBruta() {
        Venda v1 = vendaFechada(1L, "2026-09-15", new BigDecimal("1000.00"));

        when(vendaRepo.findByPeriodo(INI, FIM)).thenReturn(List.of(v1));
        when(itensRepo.somarCMVPeriodo(INI, FIM)).thenReturn(new BigDecimal("600.00"));
        when(perdasRepo.findByPeriodo(INI, FIM)).thenReturn(List.of());
        when(pagRepo.totaisPorFormaPagamento(INI, FIM)).thenReturn(List.of());
        when(itensRepo.findByVendaId(1L)).thenReturn(List.of());

        RelatorioVendasDTO dto = service.relatorioVendas(INI, FIM);

        assertThat(dto.getTotalCMV()).isEqualByComparingTo("600.00");
        assertThat(dto.getMargemBruta()).isEqualByComparingTo("400.00");
        assertThat(dto.getPercentualMargem()).isEqualByComparingTo("40.00");
    }

    @Test
    @DisplayName("relatorioVendas: ignora vendas canceladas e abertas")
    void relatorioVendas_ignoraVendasNaoFechadas() {
        Venda aberta    = vendaComStatus(3L, "2026-09-05", new BigDecimal("500.00"), "ABERTA");
        Venda cancelada = vendaComStatus(4L, "2026-09-05", new BigDecimal("500.00"), "CANCELADA");
        Venda fechada   = vendaFechada(5L, "2026-09-05", new BigDecimal("100.00"));

        when(vendaRepo.findByPeriodo(INI, FIM)).thenReturn(List.of(aberta, cancelada, fechada));
        when(itensRepo.somarCMVPeriodo(INI, FIM)).thenReturn(BigDecimal.ZERO);
        when(perdasRepo.findByPeriodo(INI, FIM)).thenReturn(List.of());
        when(pagRepo.totaisPorFormaPagamento(INI, FIM)).thenReturn(List.of());
        when(itensRepo.findByVendaId(5L)).thenReturn(List.of());

        RelatorioVendasDTO dto = service.relatorioVendas(INI, FIM);

        assertThat(dto.getQuantidadeVendas()).isEqualTo(1);
        assertThat(dto.getTotalVendas()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("relatorioVendas: agrupa vendas por dia corretamente")
    void relatorioVendas_agrupaPorDia() {
        Venda v1 = vendaFechada(1L, "2026-09-01", new BigDecimal("100.00"));
        Venda v2 = vendaFechada(2L, "2026-09-01", new BigDecimal("200.00"));
        Venda v3 = vendaFechada(3L, "2026-09-02", new BigDecimal("150.00"));

        when(vendaRepo.findByPeriodo(INI, FIM)).thenReturn(List.of(v1, v2, v3));
        when(itensRepo.somarCMVPeriodo(INI, FIM)).thenReturn(BigDecimal.ZERO);
        when(perdasRepo.findByPeriodo(INI, FIM)).thenReturn(List.of());
        when(pagRepo.totaisPorFormaPagamento(INI, FIM)).thenReturn(List.of());
        when(itensRepo.findByVendaId(anyLong())).thenReturn(List.of());

        RelatorioVendasDTO dto = service.relatorioVendas(INI, FIM);

        assertThat(dto.getVendasPorDia()).hasSize(2);
        VendaDiariaDTO dia1 = dto.getVendasPorDia().get(0);
        assertThat(dia1.getData()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(dia1.getQuantidadeVendas()).isEqualTo(2);
        assertThat(dia1.getTotalVendas()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("relatorioVendas: cria breakdown de formas de pagamento com percentual")
    void relatorioVendas_breakdownFormasPagamento() {
        Venda v1 = vendaFechada(1L, "2026-09-10", new BigDecimal("300.00"));
        // PIX=200, DINHEIRO=100 → PIX=66.67%, DINHEIRO=33.33%
        Object[] pix      = new Object[]{"PIX",     new BigDecimal("200.00")};
        Object[] dinheiro = new Object[]{"DINHEIRO", new BigDecimal("100.00")};

        when(vendaRepo.findByPeriodo(INI, FIM)).thenReturn(List.of(v1));
        when(itensRepo.somarCMVPeriodo(INI, FIM)).thenReturn(BigDecimal.ZERO);
        when(perdasRepo.findByPeriodo(INI, FIM)).thenReturn(List.of());
        when(pagRepo.totaisPorFormaPagamento(INI, FIM)).thenReturn(List.of(pix, dinheiro));
        when(itensRepo.findByVendaId(1L)).thenReturn(List.of());

        RelatorioVendasDTO dto = service.relatorioVendas(INI, FIM);

        assertThat(dto.getPorFormaPagamento()).hasSize(2);
        FormaPagamentoDTO maior = dto.getPorFormaPagamento().get(0);
        assertThat(maior.getFormaPagamento()).isEqualTo("PIX");
        assertThat(maior.getPercentual()).isEqualByComparingTo("66.67");
    }

    // ── relatorioPerdas ───────────────────────────────────────────────────────

    @Test
    @DisplayName("relatorioPerdas: retorna zeros sem perdas")
    void relatorioPerdas_semPerdas_retornaZeros() {
        when(perdasRepo.findByPeriodo(INI, FIM)).thenReturn(List.of());
        when(vendaRepo.somarTotalPeriodo(INI, FIM)).thenReturn(BigDecimal.ZERO);

        RelatorioPerdasDTO dto = service.relatorioPerdas(INI, FIM);

        assertThat(dto.getTotalCusto()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.getQuantidadeRegistros()).isZero();
        assertThat(dto.getPorMotivo()).isEmpty();
    }

    @Test
    @DisplayName("relatorioPerdas: agrupa por motivo e calcula percentuais")
    void relatorioPerdas_agrupaPorMotivo() {
        PerdasEstoque vencimento1 = perda("VENCIMENTO", new BigDecimal("150.00"));
        PerdasEstoque vencimento2 = perda("VENCIMENTO", new BigDecimal("50.00"));
        PerdasEstoque avaria      = perda("AVARIA",     new BigDecimal("80.00"));

        when(perdasRepo.findByPeriodo(INI, FIM)).thenReturn(List.of(vencimento1, vencimento2, avaria));
        when(vendaRepo.somarTotalPeriodo(INI, FIM)).thenReturn(new BigDecimal("5000.00"));

        RelatorioPerdasDTO dto = service.relatorioPerdas(INI, FIM);

        assertThat(dto.getTotalCusto()).isEqualByComparingTo("280.00");
        assertThat(dto.getQuantidadeRegistros()).isEqualTo(3);
        // VENCIMENTO = R$200 = 71.43%
        RelatorioPerdasDTO.PerdaMotivoDTO motivo1 = dto.getPorMotivo().get(0);
        assertThat(motivo1.getMotivo()).isEqualTo("VENCIMENTO");
        assertThat(motivo1.getQuantidade()).isEqualTo(2);
        assertThat(motivo1.getCusto()).isEqualByComparingTo("200.00");
        assertThat(motivo1.getPercentual()).isEqualByComparingTo("71.43");
    }

    @Test
    @DisplayName("relatorioPerdas: calcula impacto sobre receita do período")
    void relatorioPerdas_calculaImpactoReceita() {
        PerdasEstoque p = perda("FURTO", new BigDecimal("100.00"));
        when(perdasRepo.findByPeriodo(INI, FIM)).thenReturn(List.of(p));
        when(vendaRepo.somarTotalPeriodo(INI, FIM)).thenReturn(new BigDecimal("1000.00"));

        RelatorioPerdasDTO dto = service.relatorioPerdas(INI, FIM);

        // 100 / 1000 = 10%
        assertThat(dto.getPercentualImpactoVendas()).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("relatorioPerdas: impacto zero quando não há vendas no período")
    void relatorioPerdas_impactoZero_semReceita() {
        when(perdasRepo.findByPeriodo(INI, FIM)).thenReturn(List.of(perda("AVARIA", new BigDecimal("50.00"))));
        when(vendaRepo.somarTotalPeriodo(INI, FIM)).thenReturn(BigDecimal.ZERO);

        RelatorioPerdasDTO dto = service.relatorioPerdas(INI, FIM);

        assertThat(dto.getPercentualImpactoVendas()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── relatorioEstoque ──────────────────────────────────────────────────────

    @Test
    @DisplayName("relatorioEstoque: calcula valor total do estoque (qtd × custo)")
    void relatorioEstoque_calculaValorTotal() {
        // picanha: 10 kg × R$45 = R$450 | contrafilé: 3 kg × R$30 = R$90 → total = R$540
        when(produtoRepo.findAllByAtivoTrue()).thenReturn(List.of(picanha, contrafile));
        when(produtoRepo.findEstoqueAbaixoMinimo()).thenReturn(List.of(contrafile));

        RelatorioEstoqueDTO dto = service.relatorioEstoque();

        assertThat(dto.getValorTotalEstoque()).isEqualByComparingTo("540.00");
        assertThat(dto.getQuantidadeProdutos()).isEqualTo(2);
    }

    @Test
    @DisplayName("relatorioEstoque: lista alertas de estoque abaixo do mínimo")
    void relatorioEstoque_listaAlertasAbaixoMinimo() {
        // contrafilé: atual=3, mínimo=8 → diferença=-5
        when(produtoRepo.findAllByAtivoTrue()).thenReturn(List.of(picanha, contrafile));
        when(produtoRepo.findEstoqueAbaixoMinimo()).thenReturn(List.of(contrafile));

        RelatorioEstoqueDTO dto = service.relatorioEstoque();

        assertThat(dto.getProdutosAbaixoMinimo()).isEqualTo(1);
        assertThat(dto.getAlertas()).hasSize(1);
        RelatorioEstoqueDTO.AlertaEstoqueDTO alerta = dto.getAlertas().get(0);
        assertThat(alerta.getNome()).isEqualTo("Contrafilé");
        assertThat(alerta.getDiferenca()).isEqualByComparingTo("-5.000");
    }

    @Test
    @DisplayName("relatorioEstoque: sem alertas quando estoque está OK")
    void relatorioEstoque_semAlertas_estoqueOk() {
        when(produtoRepo.findAllByAtivoTrue()).thenReturn(List.of(picanha));
        when(produtoRepo.findEstoqueAbaixoMinimo()).thenReturn(List.of());

        RelatorioEstoqueDTO dto = service.relatorioEstoque();

        assertThat(dto.getProdutosAbaixoMinimo()).isZero();
        assertThat(dto.getAlertas()).isEmpty();
    }

    @Test
    @DisplayName("relatorioEstoque: produto sem custo cadastrado não conta no valor total")
    void relatorioEstoque_produtoSemCusto_naoSomaValor() {
        Produto semCusto = Produto.builder().id(3L).nome("Sal")
                .estoqueAtual(new BigDecimal("100.000")).precoCusto(null)
                .estoqueMinimo(BigDecimal.ZERO).unidadeMedida("KG").build();

        when(produtoRepo.findAllByAtivoTrue()).thenReturn(List.of(semCusto));
        when(produtoRepo.findEstoqueAbaixoMinimo()).thenReturn(List.of());

        RelatorioEstoqueDTO dto = service.relatorioEstoque();

        assertThat(dto.getValorTotalEstoque()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── relatorioContasPagar ──────────────────────────────────────────────────

    @Test
    @DisplayName("relatorioContasPagar: classifica corretamente vencidas vs a vencer")
    void relatorioContasPagar_classificaVencidaEAVencer() {
        LocalDate hoje = LocalDate.of(2026, 9, 3);

        // Vencida: venceu em 01/09
        ContasPagar vencida = ContasPagar.builder().id(1L)
                .valor(new BigDecimal("300.00")).valorPago(BigDecimal.ZERO)
                .status("ABERTO").dataVencimento(LocalDate.of(2026, 9, 1))
                .categoria("Fornecedor").build();

        // A vencer em 7 dias: vence em 08/09
        ContasPagar aVencer7 = ContasPagar.builder().id(2L)
                .valor(new BigDecimal("200.00")).valorPago(BigDecimal.ZERO)
                .status("ABERTO").dataVencimento(LocalDate.of(2026, 9, 8))
                .categoria("Aluguel").build();

        // A vencer em 30 dias: vence em 25/09
        ContasPagar aVencer30 = ContasPagar.builder().id(3L)
                .valor(new BigDecimal("500.00")).valorPago(BigDecimal.ZERO)
                .status("ABERTO").dataVencimento(LocalDate.of(2026, 9, 25))
                .categoria("Energia").build();

        when(contasPagarRepo.findByStatusOrderByDataVencimentoAsc("ABERTO"))
                .thenReturn(List.of(vencida, aVencer7, aVencer30));

        RelatorioContasPagarDTO dto = service.relatorioContasPagar(hoje);

        assertThat(dto.getTotalAberto()).isEqualByComparingTo("1000.00");
        assertThat(dto.getTotalVencido()).isEqualByComparingTo("300.00");
        assertThat(dto.getAVencer7Dias()).isEqualByComparingTo("200.00");
        // a vencer em 30 inclui tanto a de 8/9 quanto a de 25/9
        assertThat(dto.getAVencer30Dias()).isEqualByComparingTo("700.00");
    }

    @Test
    @DisplayName("relatorioContasPagar: abate valorPago do saldo devedor")
    void relatorioContasPagar_descontaValorPago() {
        LocalDate hoje = LocalDate.of(2026, 9, 3);
        // Valor=500, já pago=150 → saldo=350
        ContasPagar parcial = ContasPagar.builder().id(1L)
                .valor(new BigDecimal("500.00")).valorPago(new BigDecimal("150.00"))
                .status("ABERTO").dataVencimento(LocalDate.of(2026, 9, 1))
                .categoria("Fornecedor").build();

        when(contasPagarRepo.findByStatusOrderByDataVencimentoAsc("ABERTO"))
                .thenReturn(List.of(parcial));

        RelatorioContasPagarDTO dto = service.relatorioContasPagar(hoje);

        assertThat(dto.getTotalAberto()).isEqualByComparingTo("350.00");
        assertThat(dto.getTotalVencido()).isEqualByComparingTo("350.00");
    }

    @Test
    @DisplayName("relatorioContasPagar: breakdown por categoria com percentuais corretos")
    void relatorioContasPagar_breakdownPorCategoria() {
        LocalDate hoje = LocalDate.of(2026, 9, 3);
        ContasPagar c1 = ContasPagar.builder().id(1L)
                .valor(new BigDecimal("600.00")).valorPago(BigDecimal.ZERO)
                .status("ABERTO").dataVencimento(hoje.plusDays(5))
                .categoria("Fornecedor").build();
        ContasPagar c2 = ContasPagar.builder().id(2L)
                .valor(new BigDecimal("400.00")).valorPago(BigDecimal.ZERO)
                .status("ABERTO").dataVencimento(hoje.plusDays(15))
                .categoria("Aluguel").build();

        when(contasPagarRepo.findByStatusOrderByDataVencimentoAsc("ABERTO"))
                .thenReturn(List.of(c1, c2));

        RelatorioContasPagarDTO dto = service.relatorioContasPagar(hoje);

        // total=1000 → Fornecedor=60%, Aluguel=40%
        assertThat(dto.getPorCategoria()).hasSize(2);
        RelatorioContasPagarDTO.ContaCategoriaDTO cat1 = dto.getPorCategoria().get(0);
        assertThat(cat1.getCategoria()).isEqualTo("Fornecedor");
        assertThat(cat1.getPercentual()).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("relatorioContasPagar: sem contas abertas retorna zeros")
    void relatorioContasPagar_semContas_retornaZeros() {
        when(contasPagarRepo.findByStatusOrderByDataVencimentoAsc("ABERTO"))
                .thenReturn(List.of());

        RelatorioContasPagarDTO dto = service.relatorioContasPagar(LocalDate.now());

        assertThat(dto.getTotalAberto()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.getTotalVencido()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.getPorCategoria()).isEmpty();
    }

    // ── fluxoCaixa ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("fluxoCaixa: saldo = recebimentos - pagamentos realizados")
    void fluxoCaixa_calculaSaldo() {
        LocalDate ini = LocalDate.of(2026, 9, 1);
        LocalDate fim = LocalDate.of(2026, 9, 30);

        Venda v1 = vendaFechada(1L, "2026-09-10", new BigDecimal("800.00"));
        ContasPagar pago = ContasPagar.builder().id(1L)
                .valorPago(new BigDecimal("300.00")).status("PAGO").build();

        when(vendaRepo.findByPeriodo(any(), any())).thenReturn(List.of(v1));
        when(contasPagarRepo.findByDataVencimentoBetweenOrderByDataVencimentoAsc(ini, fim))
                .thenReturn(List.of(pago));

        FluxoCaixaDTO dto = service.fluxoCaixa(ini, fim);

        assertThat(dto.getTotalRecebimentos()).isEqualByComparingTo("800.00");
        assertThat(dto.getTotalPagamentos()).isEqualByComparingTo("300.00");
        assertThat(dto.getSaldo()).isEqualByComparingTo("500.00");
    }

    // ── helpers de fixture ────────────────────────────────────────────────────

    private Venda vendaFechada(Long id, String data, BigDecimal total) {
        return vendaComStatus(id, data, total, "FECHADA");
    }

    private Venda vendaComStatus(Long id, String data, BigDecimal total, String status) {
        return Venda.builder()
                .id(id)
                .status(status)
                .total(total)
                .desconto(BigDecimal.ZERO)
                .dataVenda(LocalDate.parse(data).atTime(10, 0))
                .build();
    }

    private PerdasEstoque perda(String motivo, BigDecimal custo) {
        return PerdasEstoque.builder()
                .motivo(motivo)
                .custoTotal(custo)
                .quantidade(new BigDecimal("1.000"))
                .build();
    }
}
