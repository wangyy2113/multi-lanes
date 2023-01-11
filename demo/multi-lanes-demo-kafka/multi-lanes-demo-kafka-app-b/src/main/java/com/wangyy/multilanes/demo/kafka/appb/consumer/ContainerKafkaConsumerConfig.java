package com.wangyy.multilanes.demo.kafka.appb.consumer;

import com.wangyy.multilanes.demo.kafka.commons.KafkaConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by houyantao on 2023/1/4
 */
@Configuration
@Slf4j
public class ContainerKafkaConsumerConfig {
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.BROKERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "appb");
        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> listenerContainer(ConsumerFactory<String, String> consumerFactory,
                                                                                       ContainerKafkaConsumer kafkaConsumer) {
        ContainerProperties containerProperties = new ContainerProperties(KafkaConstants.TOPIC_A);
        containerProperties.setMessageListener(kafkaConsumer);
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setErrorHandler((e, c) -> {
            log.error("handle message {}, error happened...", c, e);
        });
        container.setAutoStartup(true);
        return container;
    }
}
