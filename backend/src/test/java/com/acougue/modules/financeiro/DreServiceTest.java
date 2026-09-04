package com.acougue.modules.financeiro;

import com.acougue.modules.financeiro.dto.DreDTO;
import com.acougue.repository.ItensVendaRepository;
import com.acougue.repository.VendaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DreService")
class DreServiceTest {

    @Mock VendaRepository      vendaRepo;
    @Mock ItensVendaRepository itensRepo;

    @InjectMocks DreService service;

    // ── calcular ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("calcular: período formatado como MM/YYYY")
    void calcular_periodoFormatado() {
        stubVazio(2026, 9);

        DreDTO dto = service.calcular(2026, 9, BigDecimal.ZERO);

        assertThat(dto.getPeriodo()).isEqualTo("09/2026");
    }

    @Test
    @DisplayName("calcular: lucro bruto = receita - CMV")
    void calcular_lucroBruto_receitaMenosCmv() {
        // Receita=10000, CMV=6000 → lucro bruto=4000
        stubReceita(2026, 9, new BigDecimal("10000.00"));
        stubCMV(2026, 9, new BigDecimal("6000.00"));
        stubMargens(List.of());

        DreDTO dto = service.calcular(2026, 9, BigDecimal.ZERO);

        assertThat(dto.getReceitaBruta()).isEqualByComparingTo("10000.00");
        assertThat(dto.getCmv()).isEqualByComparingTo("6000.00");
        assertThat(dto.getLucroBruto()).isEqualByComparingTo("4000.00");
    }

    @Test
    @DisplayName("calcular: lucro líquido = lucro bruto - custos operacionais")
    void calcular_lucroLiquido_descontaCustosOperacionais() {
        // Receita=10000, CMV=6000, Opex=1500 → lucro líquido=2500
        stubReceita(2026, 9, new BigDecimal("10000.00"));
        stubCMV(2026, 9, new BigDecimal("6000.00"));
        stubMargens(List.of());

        DreDTO dto = service.calcular(2026, 9, new BigDecimal("1500.00"));

        assertThat(dto.getCustosOperacionais()).isEqualByComparingTo("1500.00");
        assertThat(dto.getLucroLiquido()).isEqualByComparingTo("2500.00");
    }

    @Test
    @DisplayName("calcular: percentual lucro bruto sobre receita")
    void calcular_percentualLucroBruto() {
        // 4000 / 10000 = 40%
        stubReceita(2026, 9, new BigDecimal("10000.00"));
        stubCMV(2026, 9, new BigDecimal("6000.00"));
        stubMargens(List.of());

        DreDTO dto = service.calcular(2026, 9, BigDecimal.ZERO);

        assertThat(dto.getPercentualLucroBruto()).isEqualByComparingTo("40.00");
    }

    @Test
    @DisplayName("calcular: percentual lucro líquido sobre receita")
    void calcular_percentualLucroLiquido() {
        // Líquido=2500, Receita=10000 → 25%
        stubReceita(2026, 9, new BigDecimal("10000.00"));
        stubCMV(2026, 9, new BigDecimal("6000.00"));
        stubMargens(List.of());

        DreDTO dto = service.calcular(2026, 9, new BigDecimal("1500.00"));

        assertThat(dto.getPercentualLucroLiquido()).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("calcular: percentuais são zero quando receita é zero")
    void calcular_percentuaisZero_semReceita() {
        stubReceita(2026, 9, BigDecimal.ZERO);
        stubCMV(2026, 9, BigDecimal.ZERO);
        stubMargens(List.of());

        DreDTO dto = service.calcular(2026, 9, BigDecimal.ZERO);

        assertThat(dto.getPercentualLucroBruto()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.getPercentualLucroLiquido()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calcular: custos operacionais null equivale a zero")
    void calcular_custosNulos_equivaleZero() {
        stubReceita(2026, 9, new BigDecimal("5000.00"));
        stubCMV(2026, 9, new BigDecimal("3000.00"));
        stubMargens(List.of());

        DreDTO dto = service.calcular(2026, 9, null);

        assertThat(dto.getCustosOperacionais()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.getLucroLiquido()).isEqualByComparingTo("2000.00");
    }

    @Test
    @DisplayName("calcular: margens por produto calculadas corretamente")
    void calcular_margensPorProduto() {
        // produto_id=1, nome="Picanha", qtd=10, receita=899, cmv=450
        // margem = 899-450 = 449, percentual = 449/899 = 49.94%
        Object[] row = new Object[]{
            1L, "Picanha",
            new BigDecimal("10.000"),
            new BigDecimal("899.00"),
            new BigDecimal("450.00")
        };

        stubReceita(2026, 9, new BigDecimal("899.00"));
        stubCMV(2026, 9, new BigDecimal("450.00"));
        stubMargens(List.<Object[]>of(row));

        DreDTO dto = service.calcular(2026, 9, BigDecimal.ZERO);

        assertThat(dto.getMargensPorProduto()).hasSize(1);
        DreDTO.MargemPorProdutoDTO m = dto.getMargensPorProduto().get(0);
        assertThat(m.getNomeProduto()).isEqualTo("Picanha");
        assertThat(m.getReceita()).isEqualByComparingTo("899.00");
        assertThat(m.getCmv()).isEqualByComparingTo("450.00");
        assertThat(m.getMargem()).isEqualByComparingTo("449.00");
        assertThat(m.getPercentualMargem()).isEqualByComparingTo("49.94");
    }

    @Test
    @DisplayName("calcular: produto sem CMV tem margem igual à receita")
    void calcular_produtoSemCmv_margemIgualReceita() {
        Object[] row = new Object[]{
            2L, "Frango", new BigDecimal("5.000"), new BigDecimal("200.00"), null
        };

        stubReceita(2026, 9, new BigDecimal("200.00"));
        stubCMV(2026, 9, BigDecimal.ZERO);
        stubMargens(List.<Object[]>of(row));

        DreDTO dto = service.calcular(2026, 9, BigDecimal.ZERO);

        DreDTO.MargemPorProdutoDTO m = dto.getMargensPorProduto().get(0);
        assertThat(m.getCmv()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(m.getMargem()).isEqualByComparingTo("200.00");
        assertThat(m.getPercentualMargem()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("calcular: período de janeiro usa corretamente o primeiro/último dia")
    void calcular_janeiro_consultaInicioEFimCorretos() {
        stubVazio(2026, 1);

        service.calcular(2026, 1, BigDecimal.ZERO);

        // janeiro: 1/1 00:00 até 31/1 23:59:59
        LocalDateTime esperadoInicio = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        LocalDateTime esperadoFim    = LocalDateTime.of(2026, 1, 31, 23, 59, 59);
        verify(vendaRepo).somarTotalPeriodo(esperadoInicio, esperadoFim);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void stubVazio(int ano, int mes) {
        stubReceita(ano, mes, BigDecimal.ZERO);
        stubCMV(ano, mes, BigDecimal.ZERO);
        stubMargens(List.of());
    }

    private void stubReceita(int ano, int mes, BigDecimal valor) {
        when(vendaRepo.somarTotalPeriodo(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(valor);
    }

    private void stubCMV(int ano, int mes, BigDecimal valor) {
        when(itensRepo.somarCMVPeriodo(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(valor);
    }

    private void stubMargens(List<Object[]> rows) {
        when(itensRepo.relatorioMargemPorProduto(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(rows);
    }
}
