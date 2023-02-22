package com.wangyy.multilanes.core.kafka.consumer;

import com.wangyy.multilanes.core.kafka.node.KafkaNodeWatcher;
import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 标记 tag
 */
@Aspect
@Slf4j
public class KafkaConsumerAspect {

    private KafkaNodeWatcher nodeWatcher;

    public KafkaConsumerAspect(KafkaNodeWatcher nodeWatcher) {
        this.nodeWatcher = nodeWatcher;
    }

    @Around("execution(* org.springframework.kafka.listener.MessageListener.onMessage(..))")
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ConsumerRecord<?, ?> record = (ConsumerRecord<?, ?>) args[0];
        if (!shouldConsumeRecord(record)) {
            return null;
        }
        setFeatureTag(record);
        return joinPoint.proceed();
    }

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object interceptAnno(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ConsumerRecord<?, ?> record = (ConsumerRecord<?, ?>) args[0];
        if (!shouldConsumeRecord(record)) {
            return null;
        }
        setFeatureTag(record);
        return joinPoint.proceed();
    }

    private void setFeatureTag(ConsumerRecord record) {
        Headers headers = record.headers();
        Header featureTagObj = headers.lastHeader(FeatureTagContext.NAME);

        String featureTag;
        if (featureTagObj == null) {
            featureTag = FeatureTagContext.getDEFAULT();
        } else {
            featureTag = new String(featureTagObj.value());
        }
        //设置本次请求featureTag
        FeatureTagContext.set(featureTag);
    }

    /**
     * 不存在流量标识的话，则认为是 base 的消息
     * 1. 如果当前泳道是 feat 的话
     * 1a. 如果流量也是 feat，则消费。其他情况不消费
     * 2. 如果当前泳道是 base 的话
     * 2a. 如果流量是 base，消费
     * 2b. 如果不存在 /multilane/service/feat，消费。否则不消费
     *
     * @param record
     * @return
     */
    private boolean shouldConsumeRecord(ConsumerRecord record) {
        Headers headers = record.headers();
        String currentLane = FeatureTagContext.getDEFAULT();
        Header featureTagObj = headers.lastHeader(FeatureTagContext.NAME);
        String featureTag;
        if (featureTagObj == null) {
            featureTag = FTConstants.FEATURE_TAG_BASE_LANE_VALUE;
        } else {
            featureTag = new String(featureTagObj.value());
        }
        if (!FeatureTagContext.isBaseLine()) {
            return currentLane.equals(featureTag);
        }
        if (FTConstants.FEATURE_TAG_BASE_LANE_VALUE.equals(featureTag)) {
            return true;
        }
        return !nodeWatcher.exist(featureTag);
    }
}
