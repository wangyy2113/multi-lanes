package com.wangyy.multilanes.demo.kafka.appa;

import com.wangyy.multilanes.demo.kafka.commons.KafkaConstants;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by houyantao on 2023/1/4
 */
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<Long, String> pushDataProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.BROKERS);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        return new DefaultKafkaProducerFactory<>(props);

    }

    @Bean(name = "pushDataKafkaTemplate")
    public KafkaTemplate pushDataKafkaTemplate(ProducerFactory<Long, String> pushDataProducerFactory) {
        return new KafkaTemplate<>(pushDataProducerFactory);
    }

    @Bean(name = "userKafkaProducer")
    public KafkaProducer<String, String> userKafkaProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.BROKERS);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new KafkaProducer<>(props);
    }
}
