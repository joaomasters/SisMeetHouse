package com.acougue.config;

import com.acougue.modules.messaging.events.VendaFechadaEvent;
import com.acougue.modules.messaging.events.PixConfirmadoEvent;
import com.acougue.modules.messaging.events.AlertaEstoqueEvent;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração Kafka. Só é carregada quando kafka.enabled=true (Upstash no Railway).
 * Em desenvolvimento local deixe kafka.enabled=false (padrão) — a aplicação sobe normalmente.
 *
 * Variáveis de ambiente no Railway (obtenha no console.upstash.com):
 *   KAFKA_ENABLED=true
 *   KAFKA_BOOTSTRAP_SERVERS=<broker-url>:9092
 *   KAFKA_SASL_USERNAME=<username>
 *   KAFKA_SASL_PASSWORD=<password>
 *   KAFKA_SECURITY_PROTOCOL=SASL_SSL   (padrão para Upstash)
 */
@Configuration
@EnableKafka
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.sasl.username:}")
    private String saslUsername;

    @Value("${kafka.sasl.password:}")
    private String saslPassword;

    @Value("${kafka.security.protocol:SASL_SSL}")
    private String securityProtocol;

    // ── Configurações comuns (SASL_SSL para Upstash) ──────────────────────────

    private Map<String, Object> commonProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        if ("SASL_SSL".equalsIgnoreCase(securityProtocol)) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
            props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
                    "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                    "username=\"%s\" password=\"%s\";", saslUsername, saslPassword));
        }
        return props;
    }

    // ── KafkaAdmin (sobrescreve o auto-configurado do Spring Boot) ────────────
    // Sem isso, o KafkaAdmin usa PLAINTEXT e falha ao tentar verificar os tópicos.

    @Bean
    public KafkaAdmin kafkaAdmin() {
        KafkaAdmin admin = new KafkaAdmin(commonProps());
        admin.setFatalIfBrokerNotAvailable(false); // não derruba o app se broker demorar a responder no startup
        return admin;
    }

    // ── Producer ──────────────────────────────────────────────────────────────

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = commonProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ── Consumer genérico (VendaFechadaEvent) ─────────────────────────────────

    @Bean
    public ConsumerFactory<String, VendaFechadaEvent> vendaConsumerFactory() {
        Map<String, Object> props = commonProps();
        props.put(ConsumerConfig.GROUP_ID_CONFIG,               "acougue-erp-estoque");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,      "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES,            "com.acougue.modules.messaging.events");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS,       false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,          VendaFechadaEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VendaFechadaEvent> vendaKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, VendaFechadaEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(vendaConsumerFactory());
        return factory;
    }

    // ── Consumer PIX ─────────────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, PixConfirmadoEvent> pixConsumerFactory() {
        Map<String, Object> props = commonProps();
        props.put(ConsumerConfig.GROUP_ID_CONFIG,               "acougue-erp-pix");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,      "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES,            "com.acougue.modules.messaging.events");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS,       false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,          PixConfirmadoEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PixConfirmadoEvent> pixKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PixConfirmadoEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(pixConsumerFactory());
        return factory;
    }

    // ── Consumer Alerta ───────────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, AlertaEstoqueEvent> alertaConsumerFactory() {
        Map<String, Object> props = commonProps();
        props.put(ConsumerConfig.GROUP_ID_CONFIG,               "acougue-erp-alertas");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,      "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES,            "com.acougue.modules.messaging.events");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS,       false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,          AlertaEstoqueEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AlertaEstoqueEvent> alertaKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AlertaEstoqueEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(alertaConsumerFactory());
        return factory;
    }
}
