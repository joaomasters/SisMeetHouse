package com.acougue.modules.acesso;

import com.acougue.entity.Usuario;
import com.acougue.modules.acesso.dto.AtualizarUsuarioDTO;
import com.acougue.modules.acesso.dto.CriarUsuarioDTO;
import com.acougue.modules.acesso.dto.TrocarSenhaDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Usuario> criar(@RequestBody @Valid CriarUsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizarUsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dto));
    }

    @PutMapping("/{id}/senha")
    public ResponseEntity<Void> trocarSenha(@PathVariable Long id, @RequestBody @Valid TrocarSenhaDTO dto) {
        usuarioService.trocarSenha(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Usuario> alterarStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean ativo = Boolean.TRUE.equals(body.get("ativo"));
        return ResponseEntity.ok(usuarioService.alterarStatus(id, ativo));
    }
}