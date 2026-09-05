package com.acougue.modules.messaging.consumers;

import com.acougue.config.KafkaTopicConfig;
import com.acougue.modules.messaging.events.AlertaEstoqueEvent;
import com.acougue.modules.messaging.events.VendaFechadaEvent;
import com.acougue.modules.messaging.producers.AlertaEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class EstoqueConsumer {

    private final AlertaEventProducer alertaProducer;

    @KafkaListener(
            topics        = KafkaTopicConfig.VENDAS_FECHADAS,
            groupId       = "acougue-erp-estoque",
            containerFactory = "vendaKafkaListenerContainerFactory"
    )
    public void consumir(VendaFechadaEvent event) {
        log.info("[Kafka] EstoqueConsumer recebeu VendaFechadaEvent vendaId={} itens={}",
                event.vendaId(), event.itens().size());

        for (VendaFechadaEvent.ItemEvent item : event.itens()) {
            verificarAlerta(item);
        }
    }

    public void verificarAlerta(VendaFechadaEvent.ItemEvent item) {
        BigDecimal minimo = item.estoqueMinimo() != null ? item.estoqueMinimo() : BigDecimal.ZERO;
        if (minimo.compareTo(BigDecimal.ZERO) <= 0) return; 

        if (item.estoqueAposVenda().compareTo(minimo) < 0) {
            BigDecimal deficit = item.estoqueAposVenda().subtract(minimo); 
            alertaProducer.publicarAlerta(new AlertaEstoqueEvent(
                    item.produtoId(),
                    item.nomeProduto(),
                    item.estoqueAposVenda(),
                    minimo,
                    deficit
            ));
        }
    }
}
