package com.acougue.modules.financeiro;

import com.acougue.modules.financeiro.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/financeiro/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    /** Relatório de vendas: totais, CMV, margem, top produtos, formas de pagamento, evolução diária. */
    @GetMapping("/vendas")
    public ResponseEntity<RelatorioVendasDTO> vendas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.relatorioVendas(
                inicio.atStartOfDay(), fim.atTime(LocalTime.MAX)));
    }

    /** Fluxo de caixa: recebimentos vs pagamentos realizados no período. */
    @GetMapping("/fluxo-caixa")
    public ResponseEntity<FluxoCaixaDTO> fluxoCaixa(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.fluxoCaixa(inicio, fim));
    }

    /** Perdas de estoque por motivo (VENCIMENTO, AVARIA, FURTO, …). */
    @GetMapping("/perdas")
    public ResponseEntity<RelatorioPerdasDTO> perdas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.relatorioPerdas(
                inicio.atStartOfDay(), fim.atTime(LocalTime.MAX)));
    }

    /** Snapshot do estoque atual: valor em câmara e produtos abaixo do mínimo. */
    @GetMapping("/estoque")
    public ResponseEntity<RelatorioEstoqueDTO> estoque() {
        return ResponseEntity.ok(relatorioService.relatorioEstoque());
    }

    /** Aging de contas a pagar: vencidas, a vencer em 7 e 30 dias, por categoria. */
    @GetMapping("/contas-a-pagar")
    public ResponseEntity<RelatorioContasPagarDTO> contasPagar() {
        return ResponseEntity.ok(relatorioService.relatorioContasPagar(LocalDate.now()));
    }
}
