package com.acougue.modules.pdv;

import com.acougue.entity.Caixa;
import com.acougue.entity.Venda;
import com.acougue.modules.pdv.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/pdv")
@RequiredArgsConstructor
public class PdvController {

    private final PdvService pdvService;

    // ── Caixa ─────────────────────────────────────────────────

    @PostMapping("/caixa/abrir")
    public ResponseEntity<Caixa> abrirCaixa(
            @RequestParam Long operadorId,
            @RequestParam BigDecimal valorAbertura) {
        return ResponseEntity.ok(pdvService.abrirCaixa(operadorId, valorAbertura));
    }

    @PostMapping("/caixa/{id}/fechar")
    public ResponseEntity<Caixa> fecharCaixa(
            @PathVariable Long id,
            @RequestParam BigDecimal valorInformado) {
        return ResponseEntity.ok(pdvService.fecharCaixa(id, valorInformado));
    }

    // ── Venda ─────────────────────────────────────────────────

    @PostMapping("/vendas/abrir")
    public ResponseEntity<Venda> abrirVenda(@RequestBody @Valid AbrirVendaDTO dto) {
        return ResponseEntity.ok(pdvService.abrirVenda(dto));
    }

    @GetMapping("/barcode/{ean13}")
    public ResponseEntity<ItemVendaDTO> processarBarcode(@PathVariable String ean13) {
        return ResponseEntity.ok(pdvService.processarBarcode(ean13));
    }

    @PostMapping("/vendas/{id}/itens")
    public ResponseEntity<Venda> adicionarItem(
            @PathVariable Long id,
            @RequestBody @Valid ItemVendaDTO dto) {
        return ResponseEntity.ok(pdvService.adicionarItem(id, dto));
    }

    @DeleteMapping("/vendas/{vendaId}/itens/{itemId}")
    public ResponseEntity<Venda> removerItem(
            @PathVariable Long vendaId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(pdvService.removerItem(vendaId, itemId));
    }

    @PostMapping("/vendas/fechar")
    public ResponseEntity<Venda> fecharVenda(@RequestBody @Valid FecharVendaDTO dto) {
        return ResponseEntity.ok(pdvService.fecharVenda(dto));
    }

    @PostMapping("/vendas/{id}/cancelar")
    public ResponseEntity<Venda> cancelarVenda(@PathVariable Long id) {
        return ResponseEntity.ok(pdvService.cancelarVenda(id));
    }
}
