package com.acougue.modules.estoque;

import com.acougue.entity.*;
import com.acougue.exception.BusinessException;
import com.acougue.modules.estoque.dto.ExecutarDesossaDTO;
import com.acougue.modules.estoque.dto.FichaDesossaDTO;
import com.acougue.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DesossaService {

    private final FichaDesossaRepository     fichaRepo;
    private final ProcessoDesossaRepository  processoRepo;
    private final EstoqueService             estoqueService;
    private final RecebimentoRepository      recebimentoRepo;
    private final ProdutoRepository          produtoRepo;

    /**
     * Executa o processo de desossa em uma única transação atômica.
     * 1. Valida estoque do produto pai
     * 2. Baixa o produto pai
     * 3. Calcula e entra cada corte filho com custo rateado
     * 4. Grava o registro do processo para rastreabilidade
     */
    @Transactional
    public ProcessoDesossa executarDesossa(ExecutarDesossaDTO dto) {
        FichaDesossa ficha = fichaRepo.findById(dto.getFichaDesossaId())
                .orElseThrow(() -> new BusinessException("Ficha de desossa não encontrada: " + dto.getFichaDesossaId()));

        Produto produtoPai         = ficha.getProdutoPai();
        BigDecimal qtdEntrada      = dto.getQuantidadeKgEntrada();
        BigDecimal custoPorKg      = dto.getCustoPorKg() != null ? dto.getCustoPorKg() : BigDecimal.ZERO;
        BigDecimal custoTotalBruto = qtdEntrada.multiply(custoPorKg);
        Map<Long, BigDecimal> reais = dto.getQuantidadesReais();

        // 2. Se vinculado a uma NF, validar que a quantidade não estoura o saldo daquela nota
        RecebimentoMercadoria recebimento = null;
        if (dto.getRecebimentoId() != null) {
            recebimento = recebimentoRepo.findById(dto.getRecebimentoId())
                    .orElseThrow(() -> new BusinessException("NF de recebimento não encontrada: " + dto.getRecebimentoId()));

            BigDecimal saldo = calcularSaldoNf(recebimento, produtoPai);
            if (qtdEntrada.compareTo(saldo) > 0) {
                throw new BusinessException(String.format(
                    "Quantidade (%.3f kg) maior que o saldo disponível na NF %s para '%s' (%.3f kg restantes).",
                    qtdEntrada, recebimento.getNumeroNf(), produtoPai.getNome(), saldo));
            }
        }

        // 1ª passada: calcula previsto/real/custo de cada corte SEM mexer em estoque ainda,
        // para poder validar tudo antes de qualquer efeito colateral.
        record CorteCalculado(FichaDesossaItem item, BigDecimal qtdPrevista, BigDecimal qtdReal, BigDecimal custoRateado) {}
        List<CorteCalculado> calculados = new ArrayList<>();
        BigDecimal somaReais = BigDecimal.ZERO;

        for (FichaDesossaItem item : ficha.getItens()) {
            BigDecimal percRendimento = item.getPercentualRendimento()
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            BigDecimal qtdPrevista = qtdEntrada.multiply(percRendimento).setScale(3, RoundingMode.HALF_UP);

            BigDecimal qtdReal = (reais != null && reais.containsKey(item.getProdutoFilho().getId()))
                    ? reais.get(item.getProdutoFilho().getId())
                    : qtdPrevista;

            if (qtdReal.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(
                    "Quantidade real de '" + item.getProdutoFilho().getNome() + "' não pode ser negativa.");
            }

            BigDecimal custoRateado = custoTotalBruto.multiply(percRendimento).setScale(4, RoundingMode.HALF_UP);
            calculados.add(new CorteCalculado(item, qtdPrevista, qtdReal, custoRateado));
            somaReais = somaReais.add(qtdReal);
        }

        // Trava dura: a soma dos cortes reais nunca pode ultrapassar a quantidade de entrada
        // (que já foi validada contra o saldo da NF vinculada, se houver). Sem margem de tolerância.
        if (somaReais.compareTo(qtdEntrada) > 0) {
            throw new BusinessException(String.format(
                "A soma dos cortes reais (%.3f kg) ultrapassa a quantidade de entrada (%.3f kg). " +
                "Confira se algum peso foi digitado errado.",
                somaReais, qtdEntrada));
        }

        // 1. Baixar produto pai
        estoqueService.saida(produtoPai, qtdEntrada, "SAIDA_DESOSSA",
            "DESOSSA#" + dto.getFichaDesossaId(), dto.getUsuarioId());

        ProcessoDesossa processo = ProcessoDesossa.builder()
                .fichaDesossa(ficha)
                .quantidadeEntrada(qtdEntrada)
                .usuarioId(dto.getUsuarioId())
                .observacao(dto.getObservacao())
                .status("CONCLUIDO")
                .recebimento(recebimento)
                .resultados(new ArrayList<>())
                .build();

        // 2ª passada: agora sim, executa as entradas de estoque de cada corte já validado
        for (CorteCalculado c : calculados) {
            BigDecimal custoUnitFilho = c.qtdReal().compareTo(BigDecimal.ZERO) > 0
                    ? c.custoRateado().divide(c.qtdReal(), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            estoqueService.entrada(c.item().getProdutoFilho(), c.qtdReal(), custoUnitFilho,
                "ENTRADA_DESOSSA", "DESOSSA#" + dto.getFichaDesossaId(), dto.getUsuarioId());

            ProcessoDesossaResultado resultado = ProcessoDesossaResultado.builder()
                    .processoDesossa(processo)
                    .produtoFilho(c.item().getProdutoFilho())
                    .quantidadePrevista(c.qtdPrevista())
                    .quantidadeReal(c.qtdReal())
                    .custoRateado(c.custoRateado())
                    .build();
            processo.getResultados().add(resultado);
        }

        return processoRepo.save(processo);
    }

    /**
     * Saldo ainda disponível de um produto pai numa NF específica:
     * quantidade recebida naquele item da nota MENOS o que já foi processado
     * em desossas anteriores vinculadas a essa mesma NF.
     * Evita desossar mais do que a nota trouxe, ou processar a mesma nota duas vezes.
     */
    public BigDecimal calcularSaldoNf(RecebimentoMercadoria recebimento, Produto produtoPai) {
        BigDecimal qtdNf = recebimento.getItens().stream()
                .filter(item -> item.getProduto().getId().equals(produtoPai.getId()))
                .map(RecebimentoItem::getQuantidade)
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                    "A NF " + recebimento.getNumeroNf() + " não possui item de '" + produtoPai.getNome() + "'."));

        BigDecimal jaProcessado = processoRepo.findByRecebimentoId(recebimento.getId()).stream()
                .filter(p -> p.getFichaDesossa().getProdutoPai().getId().equals(produtoPai.getId()))
                .map(ProcessoDesossa::getQuantidadeEntrada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return qtdNf.subtract(jaProcessado);
    }

    public BigDecimal saldoDisponivelNf(Long recebimentoId, Long produtoPaiId) {
        RecebimentoMercadoria recebimento = recebimentoRepo.findById(recebimentoId)
                .orElseThrow(() -> new BusinessException("NF de recebimento não encontrada: " + recebimentoId));
        Produto produtoPai = produtoRepo.findById(produtoPaiId)
                .orElseThrow(() -> new BusinessException("Produto não encontrado: " + produtoPaiId));
        return calcularSaldoNf(recebimento, produtoPai);
    }

    public List<ProcessoDesossa> listarPorFicha(Long fichaId) {
        return processoRepo.findByFichaDesossaIdOrderByDataProcessoDesc(fichaId);
    }

    public List<FichaDesossa> listarFichas() {
        return fichaRepo.findByAtivoTrue();
    }

    public List<FichaDesossa> listarTodasFichas() {
        return fichaRepo.findAll();
    }

    public FichaDesossa buscarFicha(Long id) {
        return fichaRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Ficha não encontrada: " + id));
    }

    @Transactional
    public FichaDesossa criarFicha(FichaDesossaDTO dto) {
        Produto produtoPai = produtoRepo.findById(dto.produtoPaiId())
                .orElseThrow(() -> new BusinessException("Produto pai não encontrado: " + dto.produtoPaiId()));

        FichaDesossa ficha = FichaDesossa.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .produtoPai(produtoPai)
                .build();

        adicionarItens(ficha, dto.itens());
        return fichaRepo.save(ficha);
    }

    @Transactional
    public FichaDesossa atualizarFicha(Long id, FichaDesossaDTO dto) {
        FichaDesossa ficha = buscarFicha(id);

        if (dto.produtoPaiId() != null &&
                !dto.produtoPaiId().equals(ficha.getProdutoPai().getId())) {
            Produto novoPai = produtoRepo.findById(dto.produtoPaiId())
                    .orElseThrow(() -> new BusinessException("Produto pai não encontrado"));
            ficha.setProdutoPai(novoPai);
        }

        if (dto.nome() != null)      ficha.setNome(dto.nome());
        if (dto.descricao() != null) ficha.setDescricao(dto.descricao());

        // Substitui todos os itens (cascade + orphanRemoval cuida do DELETE)
        ficha.getItens().clear();
        adicionarItens(ficha, dto.itens());

        return fichaRepo.save(ficha);
    }

    @Transactional
    public void inativarFicha(Long id) {
        FichaDesossa ficha = buscarFicha(id);
        ficha.setAtivo(false);
        fichaRepo.save(ficha);
    }

    @Transactional
    public void reativarFicha(Long id) {
        FichaDesossa ficha = buscarFicha(id);
        ficha.setAtivo(true);
        fichaRepo.save(ficha);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private void adicionarItens(FichaDesossa ficha, List<FichaDesossaDTO.ItemDTO> itemDtos) {
        if (itemDtos == null || itemDtos.isEmpty()) return;

        BigDecimal totalPerc = BigDecimal.ZERO;
        int seq = 0;
        for (FichaDesossaDTO.ItemDTO itemDto : itemDtos) {
            Produto filho = produtoRepo.findById(itemDto.produtoFilhoId())
                    .orElseThrow(() -> new BusinessException(
                            "Produto filho não encontrado: " + itemDto.produtoFilhoId()));

            FichaDesossaItem item = FichaDesossaItem.builder()
                    .fichaDesossa(ficha)
                    .produtoFilho(filho)
                    .percentualRendimento(itemDto.percentualRendimento())
                    .sequencia(itemDto.sequencia() != null ? itemDto.sequencia() : seq)
                    .build();

            ficha.getItens().add(item);
            totalPerc = totalPerc.add(itemDto.percentualRendimento());
            seq++;
        }

        // Perda = 100% - soma dos rendimentos (pode ser 0 se render tudo)
        BigDecimal perda = BigDecimal.valueOf(100).subtract(totalPerc);
        ficha.setPerdaTotalPercentual(perda.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : perda);
    }
}