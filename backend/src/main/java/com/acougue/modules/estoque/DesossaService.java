package com.acougue.modules.estoque;

import com.acougue.entity.*;
import com.acougue.exception.BusinessException;
import com.acougue.modules.estoque.dto.ExecutarDesossaDTO;
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

        // 1. Baixar produto pai
        estoqueService.saida(produtoPai, qtdEntrada, "SAIDA_DESOSSA",
            "DESOSSA#" + dto.getFichaDesossaId(), dto.getUsuarioId());

        // 2. Processar cortes filhos
        ProcessoDesossa processo = ProcessoDesossa.builder()
                .fichaDesossa(ficha)
                .quantidadeEntrada(qtdEntrada)
                .usuarioId(dto.getUsuarioId())
                .observacao(dto.getObservacao())
                .status("CONCLUIDO")
                .resultados(new ArrayList<>())
                .build();

        for (FichaDesossaItem item : ficha.getItens()) {
            BigDecimal percRendimento = item.getPercentualRendimento()
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            BigDecimal qtdPrevista = qtdEntrada.multiply(percRendimento).setScale(3, RoundingMode.HALF_UP);

            BigDecimal qtdReal = (reais != null && reais.containsKey(item.getProdutoFilho().getId()))
                    ? reais.get(item.getProdutoFilho().getId())
                    : qtdPrevista;

            // Custo rateado pelo percentual previsto (não pelo real, para não distorcer CMV)
            BigDecimal custoRateado = custoTotalBruto.multiply(percRendimento).setScale(4, RoundingMode.HALF_UP);
            BigDecimal custoUnitFilho = qtdReal.compareTo(BigDecimal.ZERO) > 0
                    ? custoRateado.divide(qtdReal, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            estoqueService.entrada(item.getProdutoFilho(), qtdReal, custoUnitFilho,
                "ENTRADA_DESOSSA", "DESOSSA#" + dto.getFichaDesossaId(), dto.getUsuarioId());

            ProcessoDesossaResultado resultado = ProcessoDesossaResultado.builder()
                    .processoDesossa(processo)
                    .produtoFilho(item.getProdutoFilho())
                    .quantidadePrevista(qtdPrevista)
                    .quantidadeReal(qtdReal)
                    .custoRateado(custoRateado)
                    .build();
            processo.getResultados().add(resultado);
        }

        return processoRepo.save(processo);
    }

    public List<ProcessoDesossa> listarPorFicha(Long fichaId) {
        return processoRepo.findByFichaDesossaIdOrderByDataProcessoDesc(fichaId);
    }

    public List<FichaDesossa> listarFichas() {
        return fichaRepo.findByAtivoTrue();
    }
}
