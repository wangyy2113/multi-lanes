package com.wangyy.multilanes.core.kafka.consumer;

import com.wangyy.multilanes.core.kafka.KafkaNodeWatcher;
import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.support.KafkaUtils;

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
        if (featureTagObj == null || new String(featureTagObj.value()).equals(FTConstants.FEATURE_TAG_BASE_LANE_VALUE)) {
            //base-line的消息，打上当前line的tag
            featureTag = FeatureTagContext.getDEFAULT();
        } else {
            //非base-line的消息，维持tag
            featureTag = new String(featureTagObj.value());
        }
        //设置本次请求featureTag
        FeatureTagContext.set(featureTag);
    }

    /**
     * 1. 不存在流量标识，则消费消息
     * 2. 如果流量标识与当前泳道环境相同的话（feat_x 泳道收到 feat_x 流量，base 泳道收到 base 流量），则消费消息。
     * 3. 如果流量标识与当前泳道环境不同的话，假设当前 Listener 处在 cs_a 消费者组，监听 topic。
     *      a. feat_x 泳道收到消息(来自 feat_y 或者 base)，不消费
     *      b. base 泳道收到 feat_x 消息
     *          1. 如果存在 /topic/cs_a_feat_x，不消费
     *          2. 如果不存在 /topic/cs_a_feat_x，消费
     *
     * @param record
     * @return
     */
    private boolean shouldConsumeRecord(ConsumerRecord record) {
        Headers headers = record.headers();
        Header featureTagObj = headers.lastHeader(FeatureTagContext.NAME);
        if (featureTagObj == null) {
            return true;
        }
        String featureTag = new String(featureTagObj.value());
        String currentEnv = FeatureTagContext.getDEFAULT();
        if (featureTag.equals(currentEnv)) {
            return true;
        }
        if (!currentEnv.equals(FTConstants.FEATURE_TAG_BASE_LANE_VALUE)) {
            return false;
        }
        String topic = record.topic();
        String consumerGroup = KafkaUtils.getConsumerGroupId();
        String featureTopic = topic + "_" + featureTag;
        String path = KafkaNodeWatcher.path(featureTopic, consumerGroup);
        return !nodeWatcher.isExist(path);
    }
}
