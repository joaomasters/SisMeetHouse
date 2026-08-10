package com.acougue.modules.estoque;

import com.acougue.entity.InventarioFisico;
import com.acougue.entity.InventarioFisicoItem;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/estoque/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @PostMapping("/abrir")
    public ResponseEntity<InventarioFisico> abrir(@RequestBody AbrirInventarioRequest req) {
        return ResponseEntity.ok(inventarioService.abrirInventario(req.getUsuarioId(), req.getObservacao()));
    }

    @GetMapping
    public ResponseEntity<List<InventarioFisico>> listar() {
        return ResponseEntity.ok(inventarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioFisico> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.buscarPorId(id));
    }

    @GetMapping("/{id}/itens")
    public ResponseEntity<List<InventarioFisicoItem>> itens(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.listarItens(id));
    }

    @PatchMapping("/{inventarioId}/itens/{produtoId}")
    public ResponseEntity<InventarioFisicoItem> contar(
            @PathVariable Long inventarioId,
            @PathVariable Long produtoId,
            @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(inventarioService.contarItem(inventarioId, produtoId, body.get("saldoContado")));
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<InventarioFisico> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.finalizarInventario(id));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<InventarioFisico> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.cancelarInventario(id));
    }

    @Data
    static class AbrirInventarioRequest {
        private Long usuarioId;
        private String observacao;
    }
}
