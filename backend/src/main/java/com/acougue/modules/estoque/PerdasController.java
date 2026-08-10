package com.acougue.modules.estoque;

import com.acougue.entity.PerdasEstoque;
import com.acougue.modules.estoque.dto.LancarPerdaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/estoque/perdas")
@RequiredArgsConstructor
public class PerdasController {

    private final PerdasService perdasService;

    @PostMapping
    public ResponseEntity<PerdasEstoque> lancar(@RequestBody LancarPerdaDTO dto) {
        return ResponseEntity.ok(perdasService.lancarPerda(dto));
    }

    @GetMapping
    public ResponseEntity<List<PerdasEstoque>> listar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(perdasService.listarPorPeriodo(
                inicio.atStartOfDay(), fim.atTime(LocalTime.MAX)));
    }

    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<PerdasEstoque>> listarPorProduto(@PathVariable Long produtoId) {
        return ResponseEntity.ok(perdasService.listarPorProduto(produtoId));
    }
}
