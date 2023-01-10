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
        if (FeatureTagContext.isBaseLine()) {
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
        log.info("kafka producer send message to new topic {}", newTopic);
    }

    @Before("execution(* org.springframework.kafka.core.KafkaTemplate.send(..))")
    public void beforeKafkaTemplateSend(JoinPoint joinPoint) {
        log.info("start kafka template intercept");
        if (FTConstants.FEATURE_TAG_BASE_LANE_VALUE.equals(FeatureTagContext.get())) {
            return;
        }

        Object[] args = joinPoint.getArgs();
        String originTopic = null;
        if (args[0] instanceof String) {
            originTopic = (String) args[0];
        } else if (args[0] instanceof ProducerRecord) {
            originTopic = ((ProducerRecord<?, ?>) args[0]).topic();
        } else {
            log.warn("unsupported kafka template send method");
            return;
        }
        String newTopic = FeatureTagUtils.buildWithFeatureTag(originTopic, FeatureTagContext.get());
        KafkaTemplate template = (KafkaTemplate) joinPoint.getTarget();
        switch (args.length) {
            case 1:
                if (args[0] instanceof ProducerRecord) {
                    ProducerRecord<?, ?> originRecord = (ProducerRecord<?, ?>) args[0];
                    ProducerRecord<?, ?> newRecord = new ProducerRecord(newTopic, originRecord.partition(),
                            originRecord.timestamp(), originRecord.key(), originRecord.value(), originRecord.headers());
                    template.send(newRecord);
                }
                break;
            case 2:
                template.send(newTopic, args[1]);
                break;
            case 3:
                template.send(newTopic, args[1], args[2]);
                break;
            case 4:
                template.send(newTopic, (Integer) args[1], args[2], args[3]);
                break;
            case 5:
                template.send(newTopic, (Integer) args[1], (Long) args[2], args[4], args[5]);
                break;
            default:
                log.warn("not send to new topic");
        }
        log.info("kafka template send message to new topic {}", newTopic);
    }
}
