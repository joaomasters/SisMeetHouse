package com.acougue.modules.financeiro;

import com.acougue.entity.*;
import com.acougue.exception.BusinessException;
import com.acougue.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FaturamentoService {

    private final FaturamentoClienteRepository faturamentoRepo;
    private final ContasAReceberRepository     contasRepo;
    private final VendaRepository              vendaRepo;
    private final ClienteRepository            clienteRepo;

    /**
     * Gera o fechamento de faturamento de um cliente para um período.
     * Agrega todas as vendas FIADO/FATURAMENTO do período e cria
     * a conta a receber consolidada.
     */
    @Transactional
    public FaturamentoCliente gerarFechamento(Long clienteId, LocalDate inicio, LocalDate fim) {
        Cliente cliente = clienteRepo.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: " + clienteId));

        LocalDateTime dtInicio = inicio.atStartOfDay();
        LocalDateTime dtFim    = fim.atTime(LocalTime.MAX);

        List<Venda> vendas = vendaRepo.findVendasFaturamentoCliente(clienteId, dtInicio, dtFim);

        BigDecimal total = vendas.stream()
                .map(Venda::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        FaturamentoCliente fat = FaturamentoCliente.builder()
                .cliente(cliente)
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalVendas(total)
                .totalPago(BigDecimal.ZERO)
                .saldoDevedor(total)
                .status("ABERTO")
                .dataVencimento(fim.plusDays(5))
                .build();

        fat = faturamentoRepo.save(fat);

        // Cria conta a receber consolidada
        ContasAReceber conta = ContasAReceber.builder()
                .cliente(cliente)
                .faturamento(fat)
                .descricao(String.format("Faturamento %s a %s", inicio, fim))
                .valor(total)
                .dataEmissao(LocalDate.now())
                .dataVencimento(fat.getDataVencimento())
                .status("ABERTO")
                .build();
        contasRepo.save(conta);

        return fat;
    }

    @Transactional
    public ContasAReceber registrarPagamento(Long contaId, BigDecimal valorPago) {
        ContasAReceber conta = contasRepo.findById(contaId)
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada: " + contaId));

        if ("PAGO".equals(conta.getStatus())) {
            throw new BusinessException("Conta já está quitada.");
        }

        conta.setValorPago(conta.getValorPago().add(valorPago));
        conta.setDataPagamento(LocalDate.now());

        BigDecimal saldo = conta.getValor().subtract(conta.getValorPago());
        if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
            conta.setStatus("PAGO");
        } else {
            conta.setStatus("PARCIAL");
        }

        // Atualizar faturamento vinculado
        if (conta.getFaturamento() != null) {
            FaturamentoCliente fat = conta.getFaturamento();
            fat.setTotalPago(fat.getTotalPago().add(valorPago));
            fat.setSaldoDevedor(fat.getTotalVendas().subtract(fat.getTotalPago()).max(BigDecimal.ZERO));
            fat.setStatus(fat.getSaldoDevedor().compareTo(BigDecimal.ZERO) == 0 ? "QUITADO" : "PARCIAL");
            faturamentoRepo.save(fat);
        }

        return contasRepo.save(conta);
    }

    public List<ContasAReceber> listarContasCliente(Long clienteId) {
        return contasRepo.findByClienteId(clienteId);
    }

    public BigDecimal saldoAbertoCliente(Long clienteId) {
        return contasRepo.saldoAberto(clienteId);
    }

    public List<FaturamentoCliente> listarFaturamentosAbertos() {
        return faturamentoRepo.findByStatusIn(List.of("ABERTO", "PARCIAL", "VENCIDO"));
    }

    @Transactional
    public void marcarVencidos() {
        List<FaturamentoCliente> vencidos = faturamentoRepo
                .findByDataVencimentoBeforeAndStatusIn(LocalDate.now(), List.of("ABERTO", "PARCIAL"));
        vencidos.forEach(f -> {
            f.setStatus("VENCIDO");
            faturamentoRepo.save(f);
        });
    }
}
