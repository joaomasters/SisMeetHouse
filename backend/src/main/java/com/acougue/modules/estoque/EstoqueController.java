package com.acougue.modules.estoque;

import com.acougue.entity.*;
import com.acougue.modules.estoque.dto.ExecutarDesossaDTO;
import com.acougue.modules.estoque.dto.FichaDesossaDTO;
import com.acougue.repository.MovimentacaoEstoqueRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final ProdutoService                 produtoService;
    private final DesossaService                 desossaService;
    private final MovimentacaoEstoqueRepository  movRepo;

    // ── Produtos ──────────────────────────────────────────────

    @GetMapping("/produtos")
    public ResponseEntity<List<Produto>> listarProdutos(
            @RequestParam(required = false) String nome) {
        if (nome != null && !nome.isBlank()) {
            return ResponseEntity.ok(produtoService.buscarPorNome(nome));
        }
        return ResponseEntity.ok(produtoService.listarAtivos());
    }

    @GetMapping("/produtos/{id}")
    public ResponseEntity<Produto> buscarProduto(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @PostMapping("/produtos")
    public ResponseEntity<Produto> criarProduto(@RequestBody @Valid Produto produto) {
        return ResponseEntity.ok(produtoService.salvar(produto));
    }

    @PutMapping("/produtos/{id}")
    public ResponseEntity<Produto> atualizarProduto(
            @PathVariable Long id, @RequestBody @Valid Produto produto) {
        return ResponseEntity.ok(produtoService.atualizar(id, produto));
    }

    @DeleteMapping("/produtos/{id}")
    public ResponseEntity<Void> inativarProduto(@PathVariable Long id) {
        produtoService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/produtos/alertas")
    public ResponseEntity<List<Produto>> alertasEstoque() {
        return ResponseEntity.ok(produtoService.alertasEstoqueMinimo());
    }

    // ── Fichas de Desossa ──────────────────────────────────────

    @GetMapping("/fichas-desossa")
    public ResponseEntity<List<FichaDesossa>> listarFichas(
            @RequestParam(defaultValue = "false") boolean todas) {
        return ResponseEntity.ok(todas
                ? desossaService.listarTodasFichas()
                : desossaService.listarFichas());
    }

    @GetMapping("/fichas-desossa/{id}")
    public ResponseEntity<FichaDesossa> buscarFicha(@PathVariable Long id) {
        return ResponseEntity.ok(desossaService.buscarFicha(id));
    }

    @PostMapping("/fichas-desossa")
    public ResponseEntity<FichaDesossa> criarFicha(@RequestBody FichaDesossaDTO dto) {
        return ResponseEntity.ok(desossaService.criarFicha(dto));
    }

    @PutMapping("/fichas-desossa/{id}")
    public ResponseEntity<FichaDesossa> atualizarFicha(
            @PathVariable Long id, @RequestBody FichaDesossaDTO dto) {
        return ResponseEntity.ok(desossaService.atualizarFicha(id, dto));
    }

    @DeleteMapping("/fichas-desossa/{id}")
    public ResponseEntity<Void> inativarFicha(@PathVariable Long id) {
        desossaService.inativarFicha(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/fichas-desossa/{id}/reativar")
    public ResponseEntity<FichaDesossa> reativarFicha(@PathVariable Long id) {
        desossaService.reativarFicha(id);
        return ResponseEntity.ok(desossaService.buscarFicha(id));
    }

    // ── Execução de Desossa ────────────────────────────────────

    @PostMapping("/desossa/executar")
    public ResponseEntity<ProcessoDesossa> executarDesossa(
            @RequestBody @Valid ExecutarDesossaDTO dto) {
        return ResponseEntity.ok(desossaService.executarDesossa(dto));
    }

    @GetMapping("/desossa/saldo-nf")
    public ResponseEntity<java.math.BigDecimal> saldoNf(
            @RequestParam Long recebimentoId, @RequestParam Long produtoPaiId) {
        return ResponseEntity.ok(desossaService.saldoDisponivelNf(recebimentoId, produtoPaiId));
    }

    @GetMapping("/desossa/historico/{fichaId}")
    public ResponseEntity<List<ProcessoDesossa>> historicoDesossa(@PathVariable Long fichaId) {
        return ResponseEntity.ok(desossaService.listarPorFicha(fichaId));
    }

    // ── Movimentações ─────────────────────────────────────────

    @GetMapping("/movimentacoes")
    public ResponseEntity<List<MovimentacaoEstoque>> listarMovimentacoes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        LocalDateTime dtInicio = inicio.atStartOfDay();
        LocalDateTime dtFim    = fim.atTime(LocalTime.MAX);
        return ResponseEntity.ok(movRepo.findByPeriodo(dtInicio, dtFim));
    }

    @GetMapping("/movimentacoes/produto/{produtoId}")
    public ResponseEntity<List<MovimentacaoEstoque>> movimentacoesProduto(@PathVariable Long produtoId) {
        return ResponseEntity.ok(movRepo.findByProdutoIdOrderByCreatedAtDesc(produtoId));
    }
}