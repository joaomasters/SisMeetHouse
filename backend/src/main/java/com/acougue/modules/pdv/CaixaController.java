package com.acougue.modules.pdv;

import com.acougue.entity.SangriaCaixa;
import com.acougue.modules.pdv.dto.FechamentoCaixaDetalhadoDTO;
import com.acougue.modules.pdv.dto.SangriaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pdv/caixa")
@RequiredArgsConstructor
public class CaixaController {

    private final CaixaService caixaService;

    @PostMapping("/{id}/sangria")
    public ResponseEntity<SangriaCaixa> sangria(
            @PathVariable Long id,
            @RequestBody SangriaDTO dto) {
        dto.setTipo("SANGRIA");
        return ResponseEntity.ok(caixaService.registrarMovimento(id, dto));
    }

    @PostMapping("/{id}/suprimento")
    public ResponseEntity<SangriaCaixa> suprimento(
            @PathVariable Long id,
            @RequestBody SangriaDTO dto) {
        dto.setTipo("SUPRIMENTO");
        return ResponseEntity.ok(caixaService.registrarMovimento(id, dto));
    }

    @GetMapping("/{id}/fechamento")
    public ResponseEntity<FechamentoCaixaDetalhadoDTO> fechamento(@PathVariable Long id) {
        return ResponseEntity.ok(caixaService.calcularFechamento(id));
    }

    @GetMapping("/{id}/movimentos")
    public ResponseEntity<List<SangriaCaixa>> movimentos(@PathVariable Long id) {
        return ResponseEntity.ok(caixaService.listarMovimentos(id));
    }
}
