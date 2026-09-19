package com.acougue.modules.balanca;

import com.acougue.entity.CargaAgendada;
import com.acougue.entity.Produto;
import com.acougue.repository.CargaAgendadaRepository;
import com.acougue.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalancaSchedulerService {

    private final ProdutoRepository            produtoRepo;
    private final CargaAgendadaRepository      cargaAgendadaRepo;
    private final BalancaArquivoGerador        gerador;
    private final com.acougue.modules.balanca.ItemPendenteBalancaService itemPendenteBalancaService;

    @Value("${balanca.scheduler.tipo:TOLEDO_MGV7}")
    private String tipoBalanca;

    @Value("${balanca.scheduler.ip:}")
    private String ipBalanca;

    @Value("${balanca.scheduler.porta:8000}")
    private Integer portaBalanca;

    @Scheduled(cron = "${balanca.scheduler.cron:0 0 7 * * *}")
    @Transactional
    public void executarCargaDiaria() {
        log.info("[Balanca] Iniciando verificacao diaria de precos...");

        long pendentes = itemPendenteBalancaService.contarPendentes();

        if (pendentes == 0) {
            log.info("[Balanca] Nenhum item pendente na fila de precos. Carga ignorada.");
            return;
        }

        log.info("[Balanca] {} item(ns) pendente(s) na fila de precos. Gerando carga...", pendentes);
        gerarCarga();
    }

    @Transactional
    public CargaAgendada gerarCargaManual() {
        log.info("[Balanca] Carga manual solicitada.");
        return gerarCarga();
    }

    private CargaAgendada gerarCarga() {
        List<Produto> produtos = produtoRepo.findByCodigoBalancaIsNotNullAndAtivoTrue();

        if (produtos.isEmpty()) {
            log.warn("[Balanca] Nenhum produto com codigo_balanca cadastrado. Abortando.");
            return null;
        }

        String conteudo = switch (tipoBalanca.toUpperCase()) {
            case "FILIZOLA_SMART" -> gerador.gerarFilizolaSmart(produtos);
            default               -> gerador.gerarToledoMGV7(produtos);
        };

        CargaAgendada carga = CargaAgendada.builder()
                .tipoBalanca(tipoBalanca)
                .status("PENDENTE")
                .produtosCount(produtos.size())
                .conteudoCarga(conteudo)
                .ipBalanca(ipBalanca.isBlank() ? null : ipBalanca)
                .portaBalanca(portaBalanca)
                .build();

        CargaAgendada salva = cargaAgendadaRepo.save(carga);
        itemPendenteBalancaService.marcarPendentesComoEnviados(salva);
        log.info("[Balanca] Carga #{} criada com {} produtos. Aguardando agente local.",
                salva.getId(), produtos.size());
        return salva;
    }
}
