package com.acougue.modules.financeiro;

import com.acougue.entity.ContasPagar;
import com.acougue.entity.ItensVenda;
import com.acougue.entity.PerdasEstoque;
import com.acougue.entity.Venda;
import com.acougue.modules.financeiro.dto.FluxoCaixaDTO;
import com.acougue.modules.financeiro.dto.ProdutoVendaDTO;
import com.acougue.modules.financeiro.dto.RelatorioVendasDTO;
import com.acougue.repository.ContasPagarRepository;
import com.acougue.repository.ItensVendaRepository;
import com.acougue.repository.PerdasEstoqueRepository;
import com.acougue.repository.VendaRepository;
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

    public RelatorioVendasDTO relatorioVendas(LocalDateTime inicio, LocalDateTime fim) {
        List<Venda> vendas = vendaRepo.findByPeriodo(inicio, fim).stream()
                .filter(v -> "FECHADA".equals(v.getStatus()))
                .collect(Collectors.toList());

        BigDecimal totalVendas = vendas.stream()
                .map(Venda::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ticketMedio = vendas.isEmpty() ? BigDecimal.ZERO :
                totalVendas.divide(BigDecimal.valueOf(vendas.size()), 2, RoundingMode.HALF_UP);

        Map<Long, ProdutoVendaDTO> produtoMap = new LinkedHashMap<>();
        for (Venda venda : vendas) {
            for (ItensVenda item : itensRepo.findByVendaId(venda.getId())) {
                Long pid = item.getProduto().getId();
                produtoMap.compute(pid, (k, existing) -> {
                    if (existing == null) {
                        return ProdutoVendaDTO.builder()
                                .produtoId(pid)
                                .nomeProduto(item.getProduto().getNome())
                                .quantidadeTotal(item.getQuantidade())
                                .valorTotal(item.getTotalItem())
                                .build();
                    }
                    existing.setQuantidadeTotal(existing.getQuantidadeTotal().add(item.getQuantidade()));
                    existing.setValorTotal(existing.getValorTotal().add(item.getTotalItem()));
                    return existing;
                });
            }
        }

        List<ProdutoVendaDTO> topProdutos = produtoMap.values().stream()
                .sorted(Comparator.comparing(ProdutoVendaDTO::getValorTotal).reversed())
                .limit(10)
                .collect(Collectors.toList());

        BigDecimal totalPerdas = perdasRepo.findByPeriodo(inicio, fim).stream()
                .map(p -> p.getCustoTotal() != null ? p.getCustoTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RelatorioVendasDTO.builder()
                .totalVendas(totalVendas)
                .quantidadeVendas(vendas.size())
                .ticketMedio(ticketMedio)
                .totalPerdas(totalPerdas)
                .topProdutos(topProdutos)
                .build();
    }

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
}
