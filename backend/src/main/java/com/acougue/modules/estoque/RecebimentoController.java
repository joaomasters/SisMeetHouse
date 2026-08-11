package com.acougue.modules.estoque;

import com.acougue.entity.RecebimentoMercadoria;
import com.acougue.modules.estoque.dto.RecebimentoDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/estoque/recebimentos")
@RequiredArgsConstructor
public class RecebimentoController {

    private final RecebimentoService recebimentoService;

    @GetMapping
    public ResponseEntity<List<RecebimentoMercadoria>> listar() {
        return ResponseEntity.ok(recebimentoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecebimentoMercadoria> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(recebimentoService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<RecebimentoMercadoria> registrar(@RequestBody @Valid RecebimentoDTO dto) {
        return ResponseEntity.ok(recebimentoService.registrar(dto));
    }

    @PutMapping("/{id}/xml")
    public ResponseEntity<RecebimentoMercadoria> uploadXml(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(recebimentoService.uploadXml(id, body.get("xml")));
    }
}
