package com.acougue.modules.financeiro;

import com.acougue.modules.financeiro.dto.DreDTO;
import com.acougue.repository.ItensVendaRepository;
import com.acougue.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DreService {

    private final VendaRepository      vendaRepo;
    private final ItensVendaRepository itensRepo;

    /**
     * Calcula o DRE simplificado para um mês/ano.
     * Receita Bruta = Σ total das vendas fechadas
     * CMV           = Σ (custo_item × quantidade) dos itens vendidos
     * Lucro Bruto   = Receita - CMV
     * Lucro Líquido = Lucro Bruto - Custos Operacionais (informados manualmente por ora)
     */
    public DreDTO calcular(int ano, int mes, BigDecimal custosOperacionais) {
        YearMonth ym = YearMonth.of(ano, mes);
        LocalDateTime inicio = ym.atDay(1).atStartOfDay();
        LocalDateTime fim    = ym.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal receitaBruta = vendaRepo.somarTotalPeriodo(inicio, fim);
        BigDecimal cmv          = itensRepo.somarCMVPeriodo(inicio, fim);
        BigDecimal lucroBruto   = receitaBruta.subtract(cmv).setScale(2, RoundingMode.HALF_UP);

        BigDecimal opex         = custosOperacionais != null ? custosOperacionais : BigDecimal.ZERO;
        BigDecimal lucroLiquido = lucroBruto.subtract(opex).setScale(2, RoundingMode.HALF_UP);

        BigDecimal pctBruto  = receitaBruta.compareTo(BigDecimal.ZERO) > 0
                ? lucroBruto.divide(receitaBruta, 4, RoundingMode.HALF_UP)
                             .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal pctLiquido = receitaBruta.compareTo(BigDecimal.ZERO) > 0
                ? lucroLiquido.divide(receitaBruta, 4, RoundingMode.HALF_UP)
                              .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Margem por produto
        List<Object[]> rawMargens = itensRepo.relatorioMargemPorProduto(inicio, fim);
        List<DreDTO.MargemPorProdutoDTO> margens = new ArrayList<>();
        for (Object[] row : rawMargens) {
            BigDecimal receita = (BigDecimal) row[3];
            BigDecimal cmvItem = row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO;
            BigDecimal margem  = receita.subtract(cmvItem).setScale(2, RoundingMode.HALF_UP);
            BigDecimal pct     = receita.compareTo(BigDecimal.ZERO) > 0
                    ? margem.divide(receita, 4, RoundingMode.HALF_UP)
                             .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            margens.add(DreDTO.MargemPorProdutoDTO.builder()
                    .produtoId((Long) row[0])
                    .nomeProduto((String) row[1])
                    .quantidadeVendida((BigDecimal) row[2])
                    .receita(receita)
                    .cmv(cmvItem)
                    .margem(margem)
                    .percentualMargem(pct)
                    .build());
        }

        return DreDTO.builder()
                .periodo(String.format("%02d/%04d", mes, ano))
                .receitaBruta(receitaBruta)
                .cmv(cmv)
                .lucroBruto(lucroBruto)
                .percentualLucroBruto(pctBruto)
                .custosOperacionais(opex)
                .lucroLiquido(lucroLiquido)
                .percentualLucroLiquido(pctLiquido)
                .margensPorProduto(margens)
                .build();
    }
}
