package com.acougue.modules.fiscal;

import com.acougue.entity.Modulo;
import com.acougue.entity.NotaFiscalSaida;
import com.acougue.modules.fiscal.dto.NotaFiscalSaidaDTO;
import com.acougue.security.Acao;
import com.acougue.security.ExigirPermissao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fiscal/notas")
@RequiredArgsConstructor
public class NotaFiscalController {

    private final NotaFiscalService notaFiscalService;

    @ExigirPermissao(modulo = Modulo.NF_SAIDA, acao = Acao.VER)
    @GetMapping
    public ResponseEntity<List<NotaFiscalSaida>> listar() {
        return ResponseEntity.ok(notaFiscalService.listar());
    }

    @ExigirPermissao(modulo = Modulo.NF_SAIDA, acao = Acao.VER)
    @GetMapping("/{id}")
    public ResponseEntity<NotaFiscalSaida> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(notaFiscalService.buscar(id));
    }

    @ExigirPermissao(modulo = Modulo.NF_SAIDA, acao = Acao.CRIAR)
    @PostMapping
    public ResponseEntity<NotaFiscalSaida> criar(@RequestBody NotaFiscalSaidaDTO dto) {
        return ResponseEntity.ok(notaFiscalService.criar(dto));
    }

    @ExigirPermissao(modulo = Modulo.NF_SAIDA, acao = Acao.EDITAR)
    @PutMapping("/{id}/status")
    public ResponseEntity<NotaFiscalSaida> status(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(notaFiscalService.atualizarStatus(id, body.get("status")));
    }

    @ExigirPermissao(modulo = Modulo.NF_SAIDA, acao = Acao.EDITAR)
    @PutMapping("/{id}/xml")
    public ResponseEntity<NotaFiscalSaida> xml(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(notaFiscalService.uploadXml(id, body.get("xml")));
    }
}