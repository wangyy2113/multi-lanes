package com.wangyy.multilanes.demo.kafka.appd.consumer;

import com.google.common.collect.Maps;
import com.wangyy.multilanes.demo.kafka.commons.KafkaConstants;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Map;

/**
 * Created by houyantao on 2023/1/5
 */
@Configuration
public class AnnoKafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> kafkaConsumerFactory() {
        Map<String, Object> props = Maps.newHashMap();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.BROKERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "appd");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(new SimpleRetryPolicy(3));
        factory.setRetryTemplate(template);
        factory.setConsumerFactory(kafkaConsumerFactory);
        return factory;
    }
}
