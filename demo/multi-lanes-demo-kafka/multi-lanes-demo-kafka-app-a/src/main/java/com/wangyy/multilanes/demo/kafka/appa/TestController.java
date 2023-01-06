package com.wangyy.multilanes.demo.kafka.appa;

import com.wangyy.multilanes.demo.kafka.commons.KafkaConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/A/test")
public class TestController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaProducer<String, String> userKafkaProducer;

    @GetMapping("/kafka")
    public Object sendTemplate(@RequestParam String message) {
        kafkaTemplate.send(KafkaConstants.TOPIC, message);
        return "suc " + System.currentTimeMillis();
    }

    @GetMapping("/kafka/producer")
    public Object sendProducer(@RequestParam String topic,
                               @RequestParam String message) {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, message);
        userKafkaProducer.send(record);
        return "suc " + System.currentTimeMillis();
    }
}
