package com.acougue.modules.financeiro;

import com.acougue.modules.financeiro.dto.FluxoCaixaDTO;
import com.acougue.modules.financeiro.dto.RelatorioVendasDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
}
