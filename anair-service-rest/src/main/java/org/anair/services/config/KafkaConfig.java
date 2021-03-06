package org.anair.services.config;

import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.spring.TracingProducerFactory;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Configuration
public class KafkaConfig {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    @Autowired
    private Tracer tracer;

    @Bean
    public ProducerFactory<String, String> producerFactory(KafkaConfigProperties kafkaConfigProperties) {
        Map<String, Object> kafkaProps = new HashMap<>();
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProperties.getBootstrapServers());
        kafkaProps.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, kafkaConfigProperties.getTransactiontimeoutMs());
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaConfigProperties.getProducer().get("key-serializer"));
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaConfigProperties.getProducer().get("value-serializer"));
        kafkaProps.put("security.protocol", kafkaConfigProperties.getSsl().get("protocol"));

        //Only on a SSL connection
        kafkaProps.put("ssl.truststore.location", kafkaConfigProperties.getSsl().get("trust.store.location"));
        kafkaProps.put("ssl.truststore.password", kafkaConfigProperties.getSsl().get("trust.store.password"));
        kafkaProps.put("ssl.keystore.location", kafkaConfigProperties.getSsl().get("key.store.location"));
        kafkaProps.put("ssl.keystore.password", kafkaConfigProperties.getSsl().get("key.store.password"));
        kafkaProps.put("ssl.key.password", kafkaConfigProperties.getSsl().get("key.password"));

        logger.trace("Kafka props: {}", kafkaProps);
        return new TracingProducerFactory<>(new DefaultKafkaProducerFactory<>(kafkaProps), tracer);
    }

    BiFunction<String, ProducerRecord, String> producerSpanNameProvider = (operationName, producerRecord) -> "PRODUCER_TOPIC";

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}
