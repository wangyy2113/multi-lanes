package com.wangyy.multilanes.demo.kafka.appb.consumer;

import com.wangyy.multilanes.demo.kafka.commons.KafkaConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Created by houyantao on 2023/1/4
 */
@Component
@Slf4j
public class ContainerKafkaConsumer implements MessageListener<String, String> {

    @Autowired
    private KafkaTemplate pushDataKafkaTemplate;

    @Override
    public void onMessage(ConsumerRecord<String, String> data) {
        String value = data.value();
        String topic = data.topic();
        log.info("container listener receive {} from {}", value, topic);
        pushDataKafkaTemplate.send(KafkaConstants.TOPIC_B, value);
    }
}
