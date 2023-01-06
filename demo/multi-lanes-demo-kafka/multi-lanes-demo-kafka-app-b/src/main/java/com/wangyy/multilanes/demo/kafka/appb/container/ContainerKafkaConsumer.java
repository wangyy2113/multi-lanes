package com.wangyy.multilanes.demo.kafka.appb.container;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Created by houyantao on 2023/1/4
 */
@Component
@Slf4j
public class ContainerKafkaConsumer implements MessageListener<String, String> {

    @Override
    public void onMessage(ConsumerRecord<String, String> data) {
        String value = data.value();
        String topic = data.topic();
        log.info("container listener receive {} from {}", value, topic);
    }
}
