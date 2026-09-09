package com.acougue.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaTopicConfig {

    
    public static final String VENDAS_FECHADAS = "acougue.vendas.fechadas";
    public static final String PIX_CONFIRMADOS = "acougue.pix.confirmados";
    public static final String ESTOQUE_ALERTAS = "acougue.estoque.alertas";

    @Bean
    public NewTopic topicVendasFechadas() {
        return TopicBuilder.name(VENDAS_FECHADAS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic topicPixConfirmados() {
        return TopicBuilder.name(PIX_CONFIRMADOS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic topicEstoqueAlertas() {
        return TopicBuilder.name(ESTOQUE_ALERTAS).partitions(1).replicas(1).build();
    }
}
