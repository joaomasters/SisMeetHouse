package com.acougue.modules.balanca;

import com.acougue.entity.CargaAgendada;
import com.acougue.entity.ItemPendenteBalanca;
import com.acougue.entity.Produto;
import com.acougue.modules.balanca.dto.ItemPendenteBalancaDTO;
import com.acougue.repository.ItemPendenteBalancaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mantém a fila de produtos com preço alterado que ainda não chegaram na
 * balança física. Um item PENDENTE é criado/atualizado toda vez que
 * {@code ProdutoService.atualizar()} detecta mudança em precoVenda para um
 * produto com codigoBalanca (PLU) cadastrado.
 *
 * Regra de negócio: só existe 1 item PENDENTE por produto por vez (garantido
 * também por índice único parcial no banco — ver V13). Se o preço mudar de
 * novo antes da carga ser efetivada, o item existente é atualizado (mantendo
 * o precoAnterior original, isto é, o preço que a balança física ainda tem
 * gravado) em vez de criar um segundo registro.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemPendenteBalancaService {

    private static final String PENDENTE = "PENDENTE";
    private static final String ENVIADO  = "ENVIADO";
    private static final String CANCELADO = "CANCELADO";

    private final ItemPendenteBalancaRepository repo;

    /**
     * Deve ser chamado logo após detectar que precoVenda mudou para um
     * produto com PLU cadastrado. Não faz nada se o produto não tiver
     * codigoBalanca (produto não é vendido na balança).
     */
    @Transactional
    public void registrarAlteracaoPreco(Produto produto, BigDecimal precoAnterior, BigDecimal precoNovo) {
        if (produto.getCodigoBalanca() == null) {
            return;
        }
        if (precoAnterior != null && precoAnterior.compareTo(precoNovo) == 0) {
            return;
        }

        ItemPendenteBalanca existente = repo.findByProdutoAndStatus(produto, PENDENTE).orElse(null);

        if (existente != null) {
            existente.setPrecoNovo(precoNovo);
            repo.save(existente);
            log.info("[Balanca] Item pendente #{} atualizado — produto={} (PLU {}), novo preco={}",
                    existente.getId(), produto.getNome(), produto.getCodigoBalanca(), precoNovo);
            return;
        }

        ItemPendenteBalanca item = ItemPendenteBalanca.builder()
                .produto(produto)
                .precoAnterior(precoAnterior != null ? precoAnterior : precoNovo)
                .precoNovo(precoNovo)
                .status(PENDENTE)
                .build();
        ItemPendenteBalanca salvo = repo.save(item);
        log.info("[Balanca] Item pendente #{} criado — produto={} (PLU {}), preco {} -> {}",
                salvo.getId(), produto.getNome(), produto.getCodigoBalanca(), precoAnterior, precoNovo);
    }

    public List<ItemPendenteBalancaDTO> listarPendentes() {
        return repo.findByStatusOrderByCriadoEmAsc(PENDENTE).stream()
                .map(ItemPendenteBalancaDTO::from)
                .toList();
    }

    public long contarPendentes() {
        return repo.countByStatus(PENDENTE);
    }

    /**
     * Chamado quando uma CargaAgendada é gerada com sucesso, para vincular
     * todos os itens PENDENTES atuais a essa carga e marcá-los como ENVIADO.
     * "Enviado" aqui significa "incluído em um arquivo de carga gerado" —
     * não necessariamente já confirmado pela balança física (isso é
     * rastreado no status da própria CargaAgendada, atualizado pelo agente
     * local em /balanca/agente/confirmar).
     */
    @Transactional
    public void marcarPendentesComoEnviados(CargaAgendada carga) {
        List<ItemPendenteBalanca> pendentes = repo.findByStatusOrderByCriadoEmAsc(PENDENTE);
        if (pendentes.isEmpty()) {
            return;
        }
        List<Long> ids = pendentes.stream().map(ItemPendenteBalanca::getId).toList();
        repo.marcarComoEnviados(ids, carga);
        log.info("[Balanca] {} item(ns) pendente(s) marcado(s) como ENVIADO — carga #{}", ids.size(), carga.getId());
    }

    @Transactional
    public void cancelar(Long id) {
        repo.findById(id).ifPresent(item -> {
            item.setStatus(CANCELADO);
            repo.save(item);
        });
    }
}
