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

    
    @GetMapping("/vendas")
    public ResponseEntity<RelatorioVendasDTO> vendas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.relatorioVendas(
                inicio.atStartOfDay(), fim.atTime(LocalTime.MAX)));
    }

    
    @GetMapping("/fluxo-caixa")
    public ResponseEntity<FluxoCaixaDTO> fluxoCaixa(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.fluxoCaixa(inicio, fim));
    }

    
    @GetMapping("/perdas")
    public ResponseEntity<RelatorioPerdasDTO> perdas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.relatorioPerdas(
                inicio.atStartOfDay(), fim.atTime(LocalTime.MAX)));
    }

    
    @GetMapping("/estoque")
    public ResponseEntity<RelatorioEstoqueDTO> estoque() {
        return ResponseEntity.ok(relatorioService.relatorioEstoque());
    }

    
    @GetMapping("/contas-a-pagar")
    public ResponseEntity<RelatorioContasPagarDTO> contasPagar() {
        return ResponseEntity.ok(relatorioService.relatorioContasPagar(LocalDate.now()));
    }
}
