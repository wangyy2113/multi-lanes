package com.wangyy.multilanes.core.kafka.producer;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.nio.charset.StandardCharsets;

/**
 * 给 feature 的 topic 发消息
 */
@Aspect
@Slf4j
public class KafkaProducerAspect {

    //针对 kafkaProducer 这种形式
    @Before("execution(* org.apache.kafka.clients.producer.KafkaProducer.send(..))")
    public void beforeKafkaProducerSend(JoinPoint joinPoint) {
        ProducerRecord producerRecord = (ProducerRecord) joinPoint.getArgs()[0];
        addHeadersToRecord(producerRecord);
    }

    private void addHeadersToRecord(ProducerRecord record) {
        record.headers().add(FeatureTagContext.NAME, FeatureTagContext.get().getBytes(StandardCharsets.UTF_8));
    }
}
