package com.wangyy.multilanes.core.kafka;

import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.kafka.support.KafkaUtils;

/**
 * Created by houyantao on 2023/1/4
 */
@Slf4j
public class MultiLanesConsumerInterceptor implements RecordInterceptor {

    private KafkaNodeWatcher nodeWatcher;

    public MultiLanesConsumerInterceptor(KafkaNodeWatcher nodeWatcher) {
        this.nodeWatcher = nodeWatcher;
    }

    @Override
    public ConsumerRecord intercept(ConsumerRecord record) {
        log.info("interceptor consumer record {} from {}", record.value(), record.topic());
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
        if (shouldSkipRecord(record)) {
            log.info("skip this record, topic {}, group {}, value {}", record.topic(), KafkaUtils.getConsumerGroupId(), record.value());
            return null;
        }
        return record;
    }

    /**
     * 1. 不存在流量标识，则消费消息。
     * 2. 如果流量标识与当前泳道环境相同的话（feat 泳道收到 feat 流量，base 泳道收到 base 流量），则消费消息。
     * 3. 如果流量标识与当前泳道环境不同的话（eg: base 泳道收到了 feat 流量），假设当前 Listener 处在 cs_a 消费者组，监听 topic_a，流量标识是 feat_x。
     * 3.a. 存在 /topic_a_feat_x/cs_a 节点，不消费。
     * 3.b. 不存在/topic_a_feat_x/cs_a 节点，消费。
     */
    private boolean shouldSkipRecord(ConsumerRecord record) {
        Headers headers = record.headers();
        Header featureTagObj = headers.lastHeader(FeatureTagContext.NAME);
        if (featureTagObj == null) {
            return false;
        }
        String featureTag = new String(featureTagObj.value());
        String currentEnv = FeatureTagContext.getDEFAULT();
        if (featureTag.equals(currentEnv)) {
            return false;
        }
        String topic = record.topic();
        String consumerGroup = KafkaUtils.getConsumerGroupId();
        String featureTopic = topic + "_" + featureTag;
        String path = KafkaNodeWatcher.path(featureTopic, consumerGroup);
        return nodeWatcher.isExist(path);
    }
}
