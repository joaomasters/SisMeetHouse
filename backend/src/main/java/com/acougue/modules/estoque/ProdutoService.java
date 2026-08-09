package com.acougue.modules.estoque;

import com.acougue.entity.Produto;
import com.acougue.exception.BusinessException;
import com.acougue.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepo;

    public List<Produto> listarAtivos() {
        return produtoRepo.findByAtivoTrue();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));
    }

    public Produto buscarPorEan13(String ean13) {
        return produtoRepo.findByEan13(ean13)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado para EAN: " + ean13));
    }

    public Produto buscarPorCodigoBalanca(Integer codigo) {
        return produtoRepo.findByCodigoBalanca(codigo)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado para PLU: " + codigo));
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoRepo.buscarPorNome(nome);
    }

    public List<Produto> listarParaBalanca() {
        return produtoRepo.findByCodigoBalancaIsNotNullAndAtivoTrue();
    }

    public List<Produto> alertasEstoqueMinimo() {
        return produtoRepo.findEstoqueAbaixoMinimo();
    }

    @Transactional
    public Produto salvar(Produto produto) {
        if (produto.getCodigoInterno() == null || produto.getCodigoInterno().isBlank()) {
            produto.setCodigoInterno(gerarCodigoInterno());
        }
        return produtoRepo.save(produto);
    }

    @Transactional
    public Produto atualizar(Long id, Produto dados) {
        Produto existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setDescricao(dados.getDescricao());
        existente.setPrecoVenda(dados.getPrecoVenda());
        existente.setPrecoCusto(dados.getPrecoCusto());
        existente.setUnidadeMedida(dados.getUnidadeMedida());
        existente.setTipoProduto(dados.getTipoProduto());
        existente.setCodigoBalanca(dados.getCodigoBalanca());
        existente.setEan13(dados.getEan13());
        existente.setEstoqueMinimo(dados.getEstoqueMinimo());
        existente.setCategoria(dados.getCategoria());
        existente.setAtivo(dados.getAtivo());
        return produtoRepo.save(existente);
    }

    @Transactional
    public void inativar(Long id) {
        Produto p = buscarPorId(id);
        p.setAtivo(false);
        produtoRepo.save(p);
    }

    private String gerarCodigoInterno() {
        long count = produtoRepo.count() + 1;
        return String.format("P%06d", count);
    }
}
