package com.acougue.security.auditoria;

import com.acougue.entity.LogAuditoria;
import com.acougue.entity.Modulo;
import com.acougue.security.Acao;
import com.acougue.security.ExigirPermissao;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @ExigirPermissao(modulo = Modulo.AUDITORIA, acao = Acao.VER)
    @GetMapping
    public ResponseEntity<List<LogAuditoria>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(auditoriaService.listar(inicio, fim, modulo, acao, usuarioId));
    }
}