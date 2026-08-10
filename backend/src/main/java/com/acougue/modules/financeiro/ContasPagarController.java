package com.acougue.modules.financeiro;

import com.acougue.entity.ContasPagar;
import com.acougue.modules.financeiro.dto.ContasPagarDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/financeiro/contas-pagar")
@RequiredArgsConstructor
public class ContasPagarController {

    private final ContasPagarService contasPagarService;

    @PostMapping
    public ResponseEntity<ContasPagar> criar(@RequestBody ContasPagarDTO dto) {
        return ResponseEntity.ok(contasPagarService.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<ContasPagar>> listar(
            @RequestParam(required = false) String status) {
        if (status != null) return ResponseEntity.ok(contasPagarService.listarPorStatus(status));
        return ResponseEntity.ok(contasPagarService.listarPorStatus("ABERTO"));
    }

    @GetMapping("/vencidas")
    public ResponseEntity<List<ContasPagar>> vencidas() {
        return ResponseEntity.ok(contasPagarService.listarVencidas());
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<ContasPagar>> porPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(contasPagarService.listarPorPeriodo(inicio, fim));
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<ContasPagar> pagar(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(contasPagarService.pagar(id, body.get("valor")));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        contasPagarService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
