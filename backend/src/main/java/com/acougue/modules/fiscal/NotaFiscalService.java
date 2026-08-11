package com.acougue.modules.fiscal;

import com.acougue.entity.Cliente;
import com.acougue.entity.NotaFiscalSaida;
import com.acougue.entity.NotaFiscalSaidaItem;
import com.acougue.entity.Produto;
import com.acougue.modules.fiscal.dto.NotaFiscalSaidaDTO;
import com.acougue.repository.ClienteRepository;
import com.acougue.repository.NotaFiscalSaidaRepository;
import com.acougue.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotaFiscalService {

    private final NotaFiscalSaidaRepository notaRepo;
    private final ClienteRepository         clienteRepo;
    private final ProdutoRepository         produtoRepo;

    public List<NotaFiscalSaida> listar() {
        return notaRepo.findAllByOrderByCreatedAtDesc();
    }

    public NotaFiscalSaida buscar(Long id) {
        return notaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("NF não encontrada: " + id));
    }

    @Transactional
    public NotaFiscalSaida criar(NotaFiscalSaidaDTO dto) {
        Cliente cliente = dto.clienteId() != null
                ? clienteRepo.findById(dto.clienteId())
                        .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: " + dto.clienteId()))
                : null;

        NotaFiscalSaida nf = NotaFiscalSaida.builder()
                .cliente(cliente)
                .numeroNf(dto.numeroNf())
                .serieNf(dto.serieNf() != null ? dto.serieNf() : "1")
                .naturezaOperacao(dto.naturezaOperacao() != null ? dto.naturezaOperacao() : "VENDA DE MERCADORIAS")
                .observacao(dto.observacao())
                .status("PENDENTE")
                .build();

        BigDecimal totalProdutos = BigDecimal.ZERO;

        if (dto.itens() != null) {
            for (NotaFiscalSaidaDTO.ItemDTO item : dto.itens()) {
                Produto produto = item.produtoId() != null
                        ? produtoRepo.findById(item.produtoId()).orElse(null)
                        : null;

                BigDecimal total = item.valorUnitario().multiply(item.quantidade())
                        .setScale(2, RoundingMode.HALF_UP);
                totalProdutos = totalProdutos.add(total);

                String desc = item.descricao() != null ? item.descricao()
                        : (produto != null ? produto.getNome() : "Item");

                NotaFiscalSaidaItem nfItem = NotaFiscalSaidaItem.builder()
                        .nota(nf)
                        .produto(produto)
                        .descricao(desc)
                        .quantidade(item.quantidade())
                        .valorUnitario(item.valorUnitario())
                        .valorTotal(total)
                        .build();
                nf.getItens().add(nfItem);
            }
        }

        nf.setValorProdutos(totalProdutos);
        nf.setValorDesconto(BigDecimal.ZERO);
        nf.setValorTotal(totalProdutos);

        return notaRepo.save(nf);
    }

    @Transactional
    public NotaFiscalSaida atualizarStatus(Long id, String status) {
        NotaFiscalSaida nf = buscar(id);
        nf.setStatus(status);
        return notaRepo.save(nf);
    }

    @Transactional
    public NotaFiscalSaida uploadXml(Long id, String xml) {
        NotaFiscalSaida nf = buscar(id);
        nf.setXmlNf(xml);
        if ("PENDENTE".equals(nf.getStatus())) nf.setStatus("EMITIDA");
        return notaRepo.save(nf);
    }
}
