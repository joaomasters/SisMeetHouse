package com.acougue.modules.estoque;

import com.acougue.entity.*;
import com.acougue.exception.BusinessException;
import com.acougue.modules.estoque.dto.ExecutarDesossaDTO;
import com.acougue.modules.estoque.dto.FichaDesossaDTO;
import com.acougue.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DesossaService")
class DesossaServiceTest {

    @Mock FichaDesossaRepository    fichaRepo;
    @Mock ProcessoDesossaRepository processoRepo;
    @Mock EstoqueService            estoqueService;
    @Mock RecebimentoRepository     recebimentoRepo;
    @Mock ProdutoRepository         produtoRepo;

    @InjectMocks DesossaService service;

    // ── fixtures ──────────────────────────────────────────────────────────────

    private Produto boi;
    private Produto picanha;
    private Produto contrafile;
    private FichaDesossa ficha;
    private RecebimentoMercadoria nf;

    @BeforeEach
    void setUp() {
        boi = Produto.builder().id(1L).nome("Boi Inteiro")
                .estoqueAtual(new BigDecimal("200.000")).unidadeMedida("KG").precoVenda(new BigDecimal("20.00")).build();

        picanha = Produto.builder().id(2L).nome("Picanha")
                .estoqueAtual(BigDecimal.ZERO).unidadeMedida("KG").precoVenda(new BigDecimal("89.90")).build();

        contrafile = Produto.builder().id(3L).nome("Contrafilé")
                .estoqueAtual(BigDecimal.ZERO).unidadeMedida("KG").precoVenda(new BigDecimal("59.90")).build();

        FichaDesossaItem itemPicanha = FichaDesossaItem.builder()
                .id(10L).produtoFilho(picanha)
                .percentualRendimento(new BigDecimal("15.00")).sequencia(0).build();

        FichaDesossaItem itemContrafile = FichaDesossaItem.builder()
                .id(11L).produtoFilho(contrafile)
                .percentualRendimento(new BigDecimal("20.00")).sequencia(1).build();

        ficha = FichaDesossa.builder()
                .id(1L).nome("Ficha Boi Inteiro").produtoPai(boi)
                .itens(new ArrayList<>(List.of(itemPicanha, itemContrafile)))
                .build();
        itemPicanha.setFichaDesossa(ficha);
        itemContrafile.setFichaDesossa(ficha);

        RecebimentoItem itemNf = RecebimentoItem.builder()
                .id(1L).produto(boi).quantidade(new BigDecimal("100.000")).build();

        nf = RecebimentoMercadoria.builder()
                .id(1L).numeroNf("NF-2024-001")
                .itens(new ArrayList<>(List.of(itemNf)))
                .build();
        itemNf.setRecebimento(nf);
    }

    // ── calcularSaldoNf ───────────────────────────────────────────────────────

    @Test
    @DisplayName("calcularSaldoNf: retorna quantidade total quando não há desossas anteriores")
    void calcularSaldoNf_semDesossasAnteriores_retornaQtdTotal() {
        when(processoRepo.findByRecebimentoId(1L)).thenReturn(List.of());

        BigDecimal saldo = service.calcularSaldoNf(nf, boi);

        assertThat(saldo).isEqualByComparingTo("100.000");
    }

    @Test
    @DisplayName("calcularSaldoNf: desconta o que já foi processado em outras desossas")
    void calcularSaldoNf_comDesossasAnteriores_descontaProcessado() {
        ProcessoDesossa processoAnterior = ProcessoDesossa.builder()
                .id(99L).fichaDesossa(ficha).quantidadeEntrada(new BigDecimal("30.000")).build();

        when(processoRepo.findByRecebimentoId(1L)).thenReturn(List.of(processoAnterior));

        BigDecimal saldo = service.calcularSaldoNf(nf, boi);

        assertThat(saldo).isEqualByComparingTo("70.000");
    }

    @Test
    @DisplayName("calcularSaldoNf: lança BusinessException quando produto não está na NF")
    void calcularSaldoNf_produtoNaoNaNf_lancaExcecao() {
        Produto outro = Produto.builder().id(99L).nome("Frango").build();

        assertThatThrownBy(() -> service.calcularSaldoNf(nf, outro))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não possui item");
    }

    // ── criarFicha ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("criarFicha: lança BusinessException quando produto pai não existe")
    void criarFicha_produtoPaiNaoEncontrado_lancaExcecao() {
        FichaDesossaDTO dto = new FichaDesossaDTO("Ficha Teste", null, 999L, List.of());
        when(produtoRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criarFicha(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Produto pai não encontrado");
    }

    @Test
    @DisplayName("criarFicha: calcula perdaTotalPercentual corretamente (100 - soma rendimentos)")
    void criarFicha_calculaPerdaTotalPercentual() {
        // 15% picanha + 20% contrafilé = 35% | perda = 65%
        List<FichaDesossaDTO.ItemDTO> itens = List.of(
                new FichaDesossaDTO.ItemDTO(2L, new BigDecimal("15.00"), 0),
                new FichaDesossaDTO.ItemDTO(3L, new BigDecimal("20.00"), 1)
        );
        FichaDesossaDTO dto = new FichaDesossaDTO("Ficha Boi", "Desossa padrão", 1L, itens);

        when(produtoRepo.findById(1L)).thenReturn(Optional.of(boi));
        when(produtoRepo.findById(2L)).thenReturn(Optional.of(picanha));
        when(produtoRepo.findById(3L)).thenReturn(Optional.of(contrafile));

        ArgumentCaptor<FichaDesossa> captor = ArgumentCaptor.forClass(FichaDesossa.class);
        when(fichaRepo.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.criarFicha(dto);

        assertThat(captor.getValue().getPerdaTotalPercentual()).isEqualByComparingTo("65.00");
        assertThat(captor.getValue().getItens()).hasSize(2);
    }

    @Test
    @DisplayName("criarFicha: perdaTotalPercentual é zero quando rendimentos somam 100%")
    void criarFicha_perdaZero_quandoRendimentoCompleto() {
        List<FichaDesossaDTO.ItemDTO> itens = List.of(
                new FichaDesossaDTO.ItemDTO(2L, new BigDecimal("60.00"), 0),
                new FichaDesossaDTO.ItemDTO(3L, new BigDecimal("40.00"), 1)
        );
        FichaDesossaDTO dto = new FichaDesossaDTO("Ficha Completa", null, 1L, itens);

        when(produtoRepo.findById(1L)).thenReturn(Optional.of(boi));
        when(produtoRepo.findById(2L)).thenReturn(Optional.of(picanha));
        when(produtoRepo.findById(3L)).thenReturn(Optional.of(contrafile));

        ArgumentCaptor<FichaDesossa> captor = ArgumentCaptor.forClass(FichaDesossa.class);
        when(fichaRepo.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.criarFicha(dto);

        assertThat(captor.getValue().getPerdaTotalPercentual()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("criarFicha: rendimentos >100% → perdaTotalPercentual é zero (não negativo)")
    void criarFicha_perdaNuncaNegativa() {
        List<FichaDesossaDTO.ItemDTO> itens = List.of(
                new FichaDesossaDTO.ItemDTO(2L, new BigDecimal("70.00"), 0),
                new FichaDesossaDTO.ItemDTO(3L, new BigDecimal("60.00"), 1)
        );
        FichaDesossaDTO dto = new FichaDesossaDTO("Ficha Acima", null, 1L, itens);

        when(produtoRepo.findById(1L)).thenReturn(Optional.of(boi));
        when(produtoRepo.findById(2L)).thenReturn(Optional.of(picanha));
        when(produtoRepo.findById(3L)).thenReturn(Optional.of(contrafile));

        ArgumentCaptor<FichaDesossa> captor = ArgumentCaptor.forClass(FichaDesossa.class);
        when(fichaRepo.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.criarFicha(dto);

        assertThat(captor.getValue().getPerdaTotalPercentual())
                .isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    // ── inativar / reativar ───────────────────────────────────────────────────

    @Test
    @DisplayName("inativarFicha: seta ativo=false e salva")
    void inativarFicha_setaAtivoFalse() {
        ficha.setAtivo(true);
        when(fichaRepo.findById(1L)).thenReturn(Optional.of(ficha));
        when(fichaRepo.save(any())).thenReturn(ficha);

        service.inativarFicha(1L);

        assertThat(ficha.getAtivo()).isFalse();
        verify(fichaRepo).save(ficha);
    }

    @Test
    @DisplayName("reativarFicha: seta ativo=true e salva")
    void reativarFicha_setaAtivoTrue() {
        ficha.setAtivo(false);
        when(fichaRepo.findById(1L)).thenReturn(Optional.of(ficha));
        when(fichaRepo.save(any())).thenReturn(ficha);

        service.reativarFicha(1L);

        assertThat(ficha.getAtivo()).isTrue();
        verify(fichaRepo).save(ficha);
    }

    @Test
    @DisplayName("buscarFicha: lança BusinessException quando não encontrada")
    void buscarFicha_naoEncontrada_lancaExcecao() {
        when(fichaRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarFicha(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ficha não encontrada");
    }

    // ── executarDesossa ───────────────────────────────────────────────────────

    @Test
    @DisplayName("executarDesossa: lança BusinessException quando ficha não existe")
    void executarDesossa_fichaNaoEncontrada_lancaExcecao() {
        ExecutarDesossaDTO dto = new ExecutarDesossaDTO();
        dto.setFichaDesossaId(99L);
        dto.setQuantidadeKgEntrada(new BigDecimal("50.000"));
        when(fichaRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executarDesossa(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ficha de desossa não encontrada");
    }

    @Test
    @DisplayName("executarDesossa: lança BusinessException quando quantidade excede saldo da NF")
    void executarDesossa_quantidadeExcedeSaldoNf_lancaExcecao() {
        ExecutarDesossaDTO dto = new ExecutarDesossaDTO();
        dto.setFichaDesossaId(1L);
        dto.setQuantidadeKgEntrada(new BigDecimal("150.000")); // NF só tem 100 kg
        dto.setRecebimentoId(1L);
        dto.setCustoPorKg(new BigDecimal("20.00"));
        dto.setUsuarioId(1L);

        when(fichaRepo.findById(1L)).thenReturn(Optional.of(ficha));
        when(recebimentoRepo.findById(1L)).thenReturn(Optional.of(nf));
        when(processoRepo.findByRecebimentoId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.executarDesossa(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("maior que o saldo disponível");
    }

    @Test
    @DisplayName("executarDesossa: processa cortes filhos com custos rateados")
    void executarDesossa_processaCortesFilhosComCustoRateado() {
        // Entrada: 100 kg a R$20/kg = R$2000 custo total
        // Picanha: 15% → 15 kg, custo rateado = 0.15 * 2000 = R$300 → R$20/kg
        // Contrafilé: 20% → 20 kg, custo rateado = 0.20 * 2000 = R$400 → R$20/kg
        ExecutarDesossaDTO dto = new ExecutarDesossaDTO();
        dto.setFichaDesossaId(1L);
        dto.setQuantidadeKgEntrada(new BigDecimal("100.000"));
        dto.setCustoPorKg(new BigDecimal("20.00"));
        dto.setUsuarioId(1L);

        when(fichaRepo.findById(1L)).thenReturn(Optional.of(ficha));
        when(processoRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.executarDesossa(dto);

        // Verifica baixa do produto pai
        verify(estoqueService).saida(eq(boi), eq(new BigDecimal("100.000")),
                eq("SAIDA_DESOSSA"), anyString(), eq(1L));

        // Verifica entrada dos filhos (2 cortes)
        verify(estoqueService, times(2)).entrada(any(), any(), any(),
                eq("ENTRADA_DESOSSA"), anyString(), eq(1L));
    }
}
