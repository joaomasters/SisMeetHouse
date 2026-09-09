package com.acougue.modules.messaging.producers;

import com.acougue.config.KafkaTopicConfig;
import com.acougue.modules.messaging.events.VendaFechadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class VendaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVendaFechada(VendaFechadaEvent event) {
        log.info("[Kafka] Publicando VendaFechadaEvent → topico={} vendaId={}",
                KafkaTopicConfig.VENDAS_FECHADAS, event.vendaId());
        kafkaTemplate.send(KafkaTopicConfig.VENDAS_FECHADAS,
                String.valueOf(event.vendaId()), event);
    }
}
