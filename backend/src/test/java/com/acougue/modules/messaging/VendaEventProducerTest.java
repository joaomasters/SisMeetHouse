package com.acougue.modules.messaging;

import com.acougue.config.KafkaTopicConfig;
import com.acougue.modules.messaging.events.VendaFechadaEvent;
import com.acougue.modules.messaging.producers.VendaEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendaEventProducer")
class VendaEventProducerTest {

    @Mock KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks VendaEventProducer producer;

    @Test
    @DisplayName("onVendaFechada: envia ao tópico correto com a chave = vendaId")
    void onVendaFechada_enviaToPicoCorreto() {
        VendaFechadaEvent event = new VendaFechadaEvent(
                42L, 1L, new BigDecimal("250.00"), List.of());

        producer.onVendaFechada(event);

        verify(kafkaTemplate).send(
                eq(KafkaTopicConfig.VENDAS_FECHADAS),
                eq("42"),
                eq(event)
        );
    }

    @Test
    @DisplayName("onVendaFechada: chave é o vendaId como string")
    void onVendaFechada_chaveEhVendaId() {
        VendaFechadaEvent event = new VendaFechadaEvent(
                99L, 2L, new BigDecimal("100.00"), List.of());

        producer.onVendaFechada(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any());
        assertThat(keyCaptor.getValue()).isEqualTo("99");
    }

    @Test
    @DisplayName("onVendaFechada: é chamado apenas uma vez por evento")
    void onVendaFechada_chamadoUmaVez() {
        VendaFechadaEvent event = new VendaFechadaEvent(
                1L, 1L, BigDecimal.TEN, List.of());

        producer.onVendaFechada(event);

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }
}
