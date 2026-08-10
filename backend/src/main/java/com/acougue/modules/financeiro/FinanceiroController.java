package com.acougue.modules.financeiro;

import com.acougue.entity.Cliente;
import com.acougue.entity.ContasAReceber;
import com.acougue.entity.FaturamentoCliente;
import com.acougue.modules.financeiro.dto.DreDTO;
import com.acougue.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

    private final FaturamentoService  faturamentoService;
    private final DreService          dreService;
    private final ClienteRepository   clienteRepository;

    // ── Clientes ──────────────────────────────────────────────

    @GetMapping("/clientes")
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteRepository.findByAtivoTrue());
    }

    // ── Faturamento ───────────────────────────────────────────

    @PostMapping("/faturamento/fechar")
    public ResponseEntity<FaturamentoCliente> gerarFechamento(
            @RequestParam Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(faturamentoService.gerarFechamento(clienteId, inicio, fim));
    }

    @GetMapping("/faturamento/abertos")
    public ResponseEntity<List<FaturamentoCliente>> listarAbertos() {
        return ResponseEntity.ok(faturamentoService.listarFaturamentosAbertos());
    }

    // ── Contas a Receber ──────────────────────────────────────

    @GetMapping("/contas-receber/cliente/{clienteId}")
    public ResponseEntity<List<ContasAReceber>> contasCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(faturamentoService.listarContasCliente(clienteId));
    }

    @GetMapping("/contas-receber/saldo/{clienteId}")
    public ResponseEntity<BigDecimal> saldoCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(faturamentoService.saldoAbertoCliente(clienteId));
    }

    @PostMapping("/contas-receber/{contaId}/pagar")
    public ResponseEntity<ContasAReceber> registrarPagamento(
            @PathVariable Long contaId,
            @RequestParam BigDecimal valor) {
        return ResponseEntity.ok(faturamentoService.registrarPagamento(contaId, valor));
    }

    // ── DRE ───────────────────────────────────────────────────

    @GetMapping("/dre")
    public ResponseEntity<DreDTO> calcularDre(
            @RequestParam int ano,
            @RequestParam int mes,
            @RequestParam(defaultValue = "0") BigDecimal custosOperacionais) {
        return ResponseEntity.ok(dreService.calcular(ano, mes, custosOperacionais));
    }
}
