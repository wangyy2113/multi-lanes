package com.wangyy.multilanes.core.kafka;

import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.core.utils.FeatureTagUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 给 feature 的 topic 发消息
 */
@Aspect
@Slf4j
public class KafkaAspect {

    @Before("execution(* org.apache.kafka.clients.producer.Producer.send(..))")
    public void beforeKafkaProducerSend(JoinPoint joinPoint) {
        log.info("start kafka producer intercept");
        if (FTConstants.FEATURE_TAG_BASE_LANE_VALUE.equals(FeatureTagContext.get())) {
            return;
        }
        ProducerRecord producerRecord = (ProducerRecord) joinPoint.getArgs()[0];
        //generate new record
        Producer producer = (Producer) joinPoint.getTarget();
        String newTopic = FeatureTagUtils.buildWithFeatureTag(producerRecord.topic(), FeatureTagContext.get());
        ProducerRecord newRecord = new ProducerRecord(newTopic, producerRecord.partition(),
                producerRecord.timestamp(), producerRecord.key(), producerRecord.value(), producerRecord.headers());
        //send to new topic
        if (joinPoint.getArgs().length == 1) {
            producer.send(newRecord);
        } else {
            producer.send(newRecord, (Callback) joinPoint.getArgs()[1]);
        }
    }

    @Before("execution(* org.springframework.kafka.core.KafkaTemplate.send(..))")
    public void beforeKafkaTemplateSend(JoinPoint joinPoint) {
        log.info("start kafka template intercept");
        if (FTConstants.FEATURE_TAG_BASE_LANE_VALUE.equals(FeatureTagContext.get())) {
            return;
        }

        KafkaTemplate template = (KafkaTemplate) joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();
        if (args[0] instanceof String) {

        }

    }
}
