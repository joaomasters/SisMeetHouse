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

/**
 * Publica VendaFechadaEvent no Kafka APÓS o commit da transação do banco.
 *
 * O padrão @TransactionalEventListener(AFTER_COMMIT) garante que o evento só
 * vai ao Kafka se a venda foi realmente persistida — evita publicar mensagens
 * para transações que rolaram back (dual-write problem).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class VendaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Escuta o Spring ApplicationEvent publicado por PdvService.fecharVenda()
     * e reenvia ao Kafka somente após o commit bem-sucedido.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVendaFechada(VendaFechadaEvent event) {
        log.info("[Kafka] Publicando VendaFechadaEvent → topico={} vendaId={}",
                KafkaTopicConfig.VENDAS_FECHADAS, event.vendaId());
        kafkaTemplate.send(KafkaTopicConfig.VENDAS_FECHADAS,
                String.valueOf(event.vendaId()), event);
    }
}
