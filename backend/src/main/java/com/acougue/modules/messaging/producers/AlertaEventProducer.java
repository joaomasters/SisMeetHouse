package com.acougue.modules.messaging.producers;

import com.acougue.config.KafkaTopicConfig;
import com.acougue.modules.messaging.events.AlertaEstoqueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class AlertaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publicarAlerta(AlertaEstoqueEvent event) {
        log.warn("[Kafka] Publicando AlertaEstoqueEvent → produto='{}' atual={} minimo={}",
                event.nomeProduto(), event.estoqueAtual(), event.estoqueMinimo());
        kafkaTemplate.send(KafkaTopicConfig.ESTOQUE_ALERTAS,
                String.valueOf(event.produtoId()), event);
    }
}
