package com.acougue.modules.messaging.consumers;

import com.acougue.config.KafkaTopicConfig;
import com.acougue.modules.messaging.events.AlertaEstoqueEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class AlertaEstoqueConsumer {

    @KafkaListener(
            topics           = KafkaTopicConfig.ESTOQUE_ALERTAS,
            groupId          = "acougue-erp-alertas",
            containerFactory = "alertaKafkaListenerContainerFactory"
    )
    public void consumir(AlertaEstoqueEvent event) {
        log.warn("[ALERTA ESTOQUE] Produto '{}' (id={}) abaixo do mínimo: " +
                 "atual={} | mínimo={} | déficit={}",
                event.nomeProduto(), event.produtoId(),
                event.estoqueAtual(), event.estoqueMinimo(), event.deficit());

        
    }
}
