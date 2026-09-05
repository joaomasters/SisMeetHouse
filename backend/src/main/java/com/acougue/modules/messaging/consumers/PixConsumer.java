package com.acougue.modules.messaging.consumers;

import com.acougue.config.KafkaTopicConfig;
import com.acougue.modules.messaging.events.PixConfirmadoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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

        
    }
}
