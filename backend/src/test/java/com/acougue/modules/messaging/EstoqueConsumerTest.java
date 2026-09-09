package com.acougue.modules.messaging;

import com.acougue.modules.messaging.consumers.EstoqueConsumer;
import com.acougue.modules.messaging.events.AlertaEstoqueEvent;
import com.acougue.modules.messaging.events.VendaFechadaEvent;
import com.acougue.modules.messaging.producers.AlertaEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstoqueConsumer")
class EstoqueConsumerTest {

    @Mock AlertaEventProducer alertaProducer;
    @InjectMocks EstoqueConsumer consumer;

    

    @Test
    @DisplayName("verificarAlerta: publica AlertaEstoqueEvent quando estoque < mínimo")
    void verificarAlerta_publicaAlerta_quandoAbaixoDoMinimo() {
        
        VendaFechadaEvent.ItemEvent item = new VendaFechadaEvent.ItemEvent(
                1L, "Picanha",
                new BigDecimal("3.000"),
                new BigDecimal("2.000"),  
                new BigDecimal("5.000")   
        );

        consumer.verificarAlerta(item);

        ArgumentCaptor<AlertaEstoqueEvent> captor = ArgumentCaptor.forClass(AlertaEstoqueEvent.class);
        verify(alertaProducer).publicarAlerta(captor.capture());

        AlertaEstoqueEvent alerta = captor.getValue();
        assertThat(alerta.produtoId()).isEqualTo(1L);
        assertThat(alerta.nomeProduto()).isEqualTo("Picanha");
        assertThat(alerta.estoqueAtual()).isEqualByComparingTo("2.000");
        assertThat(alerta.estoqueMinimo()).isEqualByComparingTo("5.000");
        assertThat(alerta.deficit()).isEqualByComparingTo("-3.000"); 
    }

    @Test
    @DisplayName("verificarAlerta: NÃO publica alerta quando estoque >= mínimo")
    void verificarAlerta_naoPublica_quandoEstoqueOk() {
        
        VendaFechadaEvent.ItemEvent item = new VendaFechadaEvent.ItemEvent(
                2L, "Contrafilé",
                new BigDecimal("2.000"),
                new BigDecimal("10.000"), 
                new BigDecimal("5.000")   
        );

        consumer.verificarAlerta(item);

        verifyNoInteractions(alertaProducer);
    }

    @Test
    @DisplayName("verificarAlerta: NÃO publica quando estoqueMinimo é zero (produto sem mínimo)")
    void verificarAlerta_naoPublica_quandoMinimoZero() {
        VendaFechadaEvent.ItemEvent item = new VendaFechadaEvent.ItemEvent(
                3L, "Sal Grosso",
                new BigDecimal("1.000"),
                new BigDecimal("0.500"),  
                BigDecimal.ZERO           
        );

        consumer.verificarAlerta(item);

        verifyNoInteractions(alertaProducer);
    }

    @Test
    @DisplayName("verificarAlerta: NÃO publica quando estoqueMinimo é nulo")
    void verificarAlerta_naoPublica_quandoMinimoNulo() {
        VendaFechadaEvent.ItemEvent item = new VendaFechadaEvent.ItemEvent(
                4L, "Frango",
                new BigDecimal("2.000"),
                new BigDecimal("1.000"),
                null  
        );

        consumer.verificarAlerta(item);

        verifyNoInteractions(alertaProducer);
    }

    @Test
    @DisplayName("verificarAlerta: publica com déficit correto quando exatamente no limite")
    void verificarAlerta_publicaAlerta_quandoExatamenteNoLimite() {
        
        VendaFechadaEvent.ItemEvent item = new VendaFechadaEvent.ItemEvent(
                5L, "Boi",
                new BigDecimal("5.000"),
                new BigDecimal("5.000"),  
                new BigDecimal("5.000")
        );

        consumer.verificarAlerta(item);

        verifyNoInteractions(alertaProducer); 
    }

    

    @Test
    @DisplayName("consumir: processa todos os itens do evento")
    void consumir_processaTodosItens() {
        
        VendaFechadaEvent event = new VendaFechadaEvent(1L, 10L, new BigDecimal("500.00"), List.of(
                new VendaFechadaEvent.ItemEvent(1L, "Picanha",   new BigDecimal("3.000"), new BigDecimal("2.000"), new BigDecimal("5.000")),
                new VendaFechadaEvent.ItemEvent(2L, "Contrafilé", new BigDecimal("1.000"), new BigDecimal("10.000"), new BigDecimal("5.000"))
        ));

        consumer.consumir(event);

        
        verify(alertaProducer, times(1)).publicarAlerta(any());
    }

    @Test
    @DisplayName("consumir: nenhum alerta quando todos os itens estão com estoque ok")
    void consumir_nenhumAlerta_todosEstoqueOk() {
        VendaFechadaEvent event = new VendaFechadaEvent(2L, 10L, new BigDecimal("200.00"), List.of(
                new VendaFechadaEvent.ItemEvent(1L, "Picanha",   new BigDecimal("2.000"), new BigDecimal("8.000"), new BigDecimal("5.000")),
                new VendaFechadaEvent.ItemEvent(2L, "Contrafilé", new BigDecimal("1.000"), new BigDecimal("6.000"), new BigDecimal("5.000"))
        ));

        consumer.consumir(event);

        verifyNoInteractions(alertaProducer);
    }

    @Test
    @DisplayName("consumir: processa evento com lista vazia de itens sem erro")
    void consumir_eventoSemItens_semErro() {
        VendaFechadaEvent event = new VendaFechadaEvent(3L, 10L, BigDecimal.ZERO, List.of());

        consumer.consumir(event);

        verifyNoInteractions(alertaProducer);
    }
}
