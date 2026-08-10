package com.acougue.modules.financeiro;

import com.acougue.entity.ContasPagar;
import com.acougue.exception.BusinessException;
import com.acougue.modules.financeiro.dto.ContasPagarDTO;
import com.acougue.repository.ContasPagarRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContasPagarService {

    private final ContasPagarRepository contasRepo;

    public ContasPagar criar(ContasPagarDTO dto) {
        return contasRepo.save(ContasPagar.builder()
                .descricao(dto.getDescricao())
                .fornecedor(dto.getFornecedor())
                .valor(dto.getValor())
                .dataVencimento(dto.getDataVencimento())
                .categoria(dto.getCategoria())
                .observacao(dto.getObservacao())
                .build());
    }

    @Transactional
    public ContasPagar pagar(Long contaId, BigDecimal valorPago) {
        ContasPagar conta = buscar(contaId);
        if ("CANCELADO".equals(conta.getStatus()) || "PAGO".equals(conta.getStatus())) {
            throw new BusinessException("Conta já está " + conta.getStatus().toLowerCase() + ".");
        }
        BigDecimal totalPago = conta.getValorPago().add(valorPago);
        conta.setValorPago(totalPago);
        if (totalPago.compareTo(conta.getValor()) >= 0) {
            conta.setStatus("PAGO");
            conta.setDataPagamento(LocalDate.now());
        } else {
            conta.setStatus("PARCIAL");
        }
        return contasRepo.save(conta);
    }

    @Transactional
    public void cancelar(Long contaId) {
        ContasPagar conta = buscar(contaId);
        if ("PAGO".equals(conta.getStatus())) {
            throw new BusinessException("Não é possível cancelar uma conta já paga.");
        }
        conta.setStatus("CANCELADO");
        contasRepo.save(conta);
    }

    public List<ContasPagar> listarPorStatus(String status) {
        return contasRepo.findByStatusOrderByDataVencimentoAsc(status);
    }

    public List<ContasPagar> listarVencidas() {
        return contasRepo.findByDataVencimentoBeforeAndStatusOrderByDataVencimentoAsc(LocalDate.now(), "ABERTO");
    }

    public List<ContasPagar> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return contasRepo.findByDataVencimentoBetweenOrderByDataVencimentoAsc(inicio, fim);
    }

    private ContasPagar buscar(Long id) {
        return contasRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada: " + id));
    }
}
