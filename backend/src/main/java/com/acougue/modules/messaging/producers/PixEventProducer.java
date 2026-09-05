package com.acougue.modules.messaging.producers;

import com.acougue.config.KafkaTopicConfig;
import com.acougue.modules.messaging.events.PixConfirmadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica PixConfirmadoEvent quando o webhook do Mercado Pago confirma o PIX.
 * Permite que o PDV receba a confirmação em tempo real via WebSocket/SSE
 * (subscriber futuro) em vez de fazer polling.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class PixEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publicarPixConfirmado(PixConfirmadoEvent event) {
        log.info("[Kafka] Publicando PixConfirmadoEvent → topico={} vendaId={} mpId={}",
                KafkaTopicConfig.PIX_CONFIRMADOS, event.vendaId(), event.mpPaymentId());
        kafkaTemplate.send(KafkaTopicConfig.PIX_CONFIRMADOS,
                String.valueOf(event.mpPaymentId()), event);
    }
}
