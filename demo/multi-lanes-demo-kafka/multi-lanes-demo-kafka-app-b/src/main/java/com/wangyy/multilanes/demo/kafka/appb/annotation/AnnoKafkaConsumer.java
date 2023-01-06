package com.wangyy.multilanes.demo.kafka.appb.annotation;

import com.wangyy.multilanes.demo.kafka.commons.KafkaConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by houyantao on 2023/1/5
 */
@Component
@Slf4j
public class AnnoKafkaConsumer {

    @Autowired
    private ApplicationContext applicationContext;

    @KafkaListener(topics = KafkaConstants.TOPIC, containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        String topic = consumerRecord.topic();
        String value = consumerRecord.value();
        log.info("annotation listener receive message {} from {}", value, topic);
    }
}
