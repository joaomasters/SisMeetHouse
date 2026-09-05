package com.acougue.modules.messaging;

import com.acougue.config.KafkaTopicConfig;
import com.acougue.modules.messaging.events.PixConfirmadoEvent;
import com.acougue.modules.messaging.producers.PixEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PixEventProducer")
class PixEventProducerTest {

    @Mock KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks PixEventProducer producer;

    @Test
    @DisplayName("publicarPixConfirmado: envia ao tópico pix.confirmados")
    void publicarPixConfirmado_enviaToPicoCorreto() {
        PixConfirmadoEvent event = new PixConfirmadoEvent(10L, 999L, new BigDecimal("150.00"));

        producer.publicarPixConfirmado(event);

        verify(kafkaTemplate).send(
                eq(KafkaTopicConfig.PIX_CONFIRMADOS),
                eq("999"),  // chave = mpPaymentId
                eq(event)
        );
    }

    @Test
    @DisplayName("publicarPixConfirmado: chave é o mpPaymentId como string")
    void publicarPixConfirmado_chaveEhMpPaymentId() {
        PixConfirmadoEvent event = new PixConfirmadoEvent(5L, 12345L, new BigDecimal("50.00"));

        producer.publicarPixConfirmado(event);

        verify(kafkaTemplate).send(anyString(), eq("12345"), any());
    }
}
