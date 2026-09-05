package com.acougue.modules.messaging.consumers;

import com.acougue.config.KafkaTopicConfig;
import com.acougue.modules.messaging.events.PixConfirmadoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consome PixConfirmadoEvent publicado pelo webhook do Mercado Pago.
 *
 * Extensões futuras:
 *  - Push SSE/WebSocket para o PDV parar o polling
 *  - Fechar a venda automaticamente quando o PIX confirmar
 *  - Notificar o caixa via toast/som
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class PixConsumer {

    @KafkaListener(
            topics           = KafkaTopicConfig.PIX_CONFIRMADOS,
            groupId          = "acougue-erp-pix",
            containerFactory = "pixKafkaListenerContainerFactory"
    )
    public void consumir(PixConfirmadoEvent event) {
        log.info("[Kafka] PixConsumer → PIX confirmado: vendaId={} mpId={} valor={}",
                event.vendaId(), event.mpPaymentId(), event.valor());

        // TODO: notificar o PDV via WebSocket/SSE para parar o polling
    }
}
