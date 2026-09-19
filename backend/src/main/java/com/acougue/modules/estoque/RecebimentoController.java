package com.acougue.modules.estoque;

import com.acougue.entity.Modulo;
import com.acougue.entity.RecebimentoMercadoria;
import com.acougue.modules.estoque.dto.RecebimentoDTO;
import com.acougue.security.Acao;
import com.acougue.security.ExigirPermissao;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/estoque/recebimentos")
@RequiredArgsConstructor
public class RecebimentoController {

    private final RecebimentoService recebimentoService;

    @ExigirPermissao(modulo = Modulo.RECEBIMENTO, acao = Acao.VER)
    @GetMapping
    public ResponseEntity<List<RecebimentoMercadoria>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String fornecedor) {

        boolean semFiltro = inicio == null && fim == null && (fornecedor == null || fornecedor.isBlank());
        if (semFiltro) {
            // Compatibilidade: DesossaPage usa esse mesmo endpoint sem filtros
            // pra popular o combo de "vincular NF de recebimento".
            return ResponseEntity.ok(recebimentoService.listar());
        }

        LocalDateTime desde = inicio != null ? inicio.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime ate   = fim    != null ? fim.atTime(LocalTime.MAX) : LocalDateTime.now();
        return ResponseEntity.ok(recebimentoService.listar(desde, ate, fornecedor));
    }

    @ExigirPermissao(modulo = Modulo.RECEBIMENTO, acao = Acao.VER)
    @GetMapping("/{id}")
    public ResponseEntity<RecebimentoMercadoria> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(recebimentoService.buscar(id));
    }

    @ExigirPermissao(modulo = Modulo.RECEBIMENTO, acao = Acao.CRIAR)
    @PostMapping
    public ResponseEntity<RecebimentoMercadoria> registrar(@RequestBody @Valid RecebimentoDTO dto) {
        return ResponseEntity.ok(recebimentoService.registrar(dto));
    }

    @ExigirPermissao(modulo = Modulo.RECEBIMENTO, acao = Acao.EDITAR)
    @PutMapping("/{id}/xml")
    public ResponseEntity<RecebimentoMercadoria> uploadXml(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(recebimentoService.uploadXml(id, body.get("xml")));
    }
}