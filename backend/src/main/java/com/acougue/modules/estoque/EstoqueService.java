package com.acougue.modules.estoque;

import com.acougue.entity.MovimentacaoEstoque;
import com.acougue.entity.Produto;
import com.acougue.exception.BusinessException;
import com.acougue.repository.MovimentacaoEstoqueRepository;
import com.acougue.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final ProdutoRepository      produtoRepo;
    private final MovimentacaoEstoqueRepository movRepo;

    @Transactional
    public void entrada(Produto produto, BigDecimal quantidade, BigDecimal custoUnitario,
                        String tipoMov, String docRef, Long usuarioId) {
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantidade de entrada deve ser positiva.");
        }
        atualizarCustoMedio(produto, quantidade, custoUnitario);
        produto.setEstoqueAtual(produto.getEstoqueAtual().add(quantidade).setScale(4, RoundingMode.HALF_UP));
        produtoRepo.save(produto);
        registrar(produto, quantidade, custoUnitario, tipoMov, docRef, usuarioId);
    }

    @Transactional
    public void saida(Produto produto, BigDecimal quantidade,
                      String tipoMov, String docRef, Long usuarioId) {
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantidade de saída deve ser positiva.");
        }
        if (produto.getEstoqueAtual().compareTo(quantidade) < 0) {
            throw new BusinessException(String.format(
                "Estoque insuficiente para '%s'. Disponível: %.3f | Solicitado: %.3f",
                produto.getNome(), produto.getEstoqueAtual(), quantidade));
        }
        produto.setEstoqueAtual(produto.getEstoqueAtual().subtract(quantidade).setScale(4, RoundingMode.HALF_UP));
        produtoRepo.save(produto);
        registrar(produto, quantidade, produto.getPrecoCusto(), tipoMov, docRef, usuarioId);
    }

    public List<Produto> listarEstoqueAbaixoMinimo() {
        return produtoRepo.findEstoqueAbaixoMinimo();
    }

    @Transactional
    public void ajuste(Produto produto, BigDecimal quantidade, String tipo, String docRef, Long usuarioId) {
        if ("AJUSTE_POSITIVO".equals(tipo)) {
            produto.setEstoqueAtual(produto.getEstoqueAtual().add(quantidade).setScale(4, RoundingMode.HALF_UP));
        } else {
            produto.setEstoqueAtual(produto.getEstoqueAtual().subtract(quantidade).max(BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP));
        }
        produtoRepo.save(produto);
        registrar(produto, quantidade, produto.getPrecoCusto(), tipo, docRef, usuarioId);
    }

    private void atualizarCustoMedio(Produto produto, BigDecimal qtdNova, BigDecimal custoNovo) {
        if (custoNovo == null || custoNovo.compareTo(BigDecimal.ZERO) <= 0) return;
        BigDecimal estoqueAnt = produto.getEstoqueAtual();
        BigDecimal custoAnt   = produto.getPrecoCusto() != null ? produto.getPrecoCusto() : BigDecimal.ZERO;
        BigDecimal novoEstoque = estoqueAnt.add(qtdNova);
        if (novoEstoque.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal novoCusto = estoqueAnt.multiply(custoAnt)
                    .add(qtdNova.multiply(custoNovo))
                    .divide(novoEstoque, 4, RoundingMode.HALF_UP);
            produto.setPrecoCusto(novoCusto);
        }
    }

    private void registrar(Produto produto, BigDecimal quantidade, BigDecimal custo,
                           String tipo, String docRef, Long usuarioId) {
        MovimentacaoEstoque mov = MovimentacaoEstoque.builder()
                .produto(produto)
                .tipoMovimentacao(tipo)
                .quantidade(quantidade)
                .custoUnitario(custo)
                .documentoRef(docRef)
                .usuarioId(usuarioId)
                .build();
        movRepo.save(mov);
    }
}
