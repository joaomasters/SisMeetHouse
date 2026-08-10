package com.acougue.modules.estoque;

import com.acougue.entity.PerdasEstoque;
import com.acougue.entity.Produto;
import com.acougue.modules.estoque.dto.LancarPerdaDTO;
import com.acougue.repository.PerdasEstoqueRepository;
import com.acougue.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerdasService {

    private final PerdasEstoqueRepository perdasRepo;
    private final ProdutoRepository       produtoRepo;
    private final EstoqueService          estoqueService;

    @Transactional
    public PerdasEstoque lancarPerda(LancarPerdaDTO dto) {
        Produto produto = produtoRepo.findById(dto.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + dto.getProdutoId()));

        BigDecimal custoUnit = produto.getPrecoCusto() != null ? produto.getPrecoCusto() : BigDecimal.ZERO;
        BigDecimal custoTotal = custoUnit.multiply(dto.getQuantidade());

        PerdasEstoque perda = PerdasEstoque.builder()
                .produto(produto)
                .quantidade(dto.getQuantidade())
                .custoUnitario(custoUnit)
                .custoTotal(custoTotal)
                .motivo(dto.getMotivo())
                .observacao(dto.getObservacao())
                .usuarioId(dto.getUsuarioId())
                .build();

        estoqueService.saida(produto, dto.getQuantidade(),
                "SAIDA_DESCARTE", "PERDA_" + dto.getMotivo(), dto.getUsuarioId());

        return perdasRepo.save(perda);
    }

    public List<PerdasEstoque> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return perdasRepo.findByPeriodo(inicio, fim);
    }

    public List<PerdasEstoque> listarPorProduto(Long produtoId) {
        return perdasRepo.findByProdutoIdOrderByCreatedAtDesc(produtoId);
    }
}
