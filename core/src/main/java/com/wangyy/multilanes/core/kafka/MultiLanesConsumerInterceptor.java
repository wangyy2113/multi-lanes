package com.wangyy.multilanes.core.kafka;

import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.listener.RecordInterceptor;

import java.util.Arrays;

/**
 * Created by houyantao on 2023/1/4
 */
@Slf4j
public class MultiLanesConsumerInterceptor implements RecordInterceptor {

    @Override
    public ConsumerRecord intercept(ConsumerRecord record) {
        log.info("interceptor consumer record {} from {}", record.value(), record.topic());
        Headers headers = record.headers();
        Header featureTagObj = headers.lastHeader(FeatureTagContext.NAME);

        String featureTag;
        if (featureTagObj == null || Arrays.toString(featureTagObj.value()).equals(FTConstants.FEATURE_TAG_BASE_LANE_VALUE)) {
            //base-line的消息，打上当前line的tag
            featureTag = FeatureTagContext.getDEFAULT();
        } else {
            //非base-line的消息，维持tag
            featureTag = Arrays.toString(featureTagObj.value());
        }
        //设置本次请求featureTag
        FeatureTagContext.set(featureTag);
        return record;
    }

    public static MultiLanesConsumerInterceptor instance() {
        return InstanceHolder.INTERCEPTOR;
    }

    private static class InstanceHolder {
        private static final MultiLanesConsumerInterceptor INTERCEPTOR = new MultiLanesConsumerInterceptor();
    }
}
