package com.acougue.modules.messaging.consumers;

import com.acougue.config.KafkaTopicConfig;
import com.acougue.modules.messaging.events.AlertaEstoqueEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consome AlertaEstoqueEvent e toma ação (log + extensível para email/push).
 *
 * Extensões futuras deste consumer:
 *  - Enviar e-mail para o gerente
 *  - Push notification no app
 *  - Atualizar badge no dashboard em tempo real via WebSocket
 *  - Criar ordem de compra automática
 */
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

        // TODO: integrar com serviço de notificação (email, push, WebSocket)
    }
}
