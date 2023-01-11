package com.wangyy.multilanes.demo.kafka.appd.consumer;

import com.wangyy.multilanes.demo.kafka.commons.KafkaConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Created by houyantao on 2023/1/5
 */
@Component
@Slf4j
public class AnnoKafkaConsumer {


    @KafkaListener(topics = KafkaConstants.TOPIC_B, containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        String topic = consumerRecord.topic();
        String value = consumerRecord.value();
        log.info("annotation listener receive message {} from {}", value, topic);
    }
}
