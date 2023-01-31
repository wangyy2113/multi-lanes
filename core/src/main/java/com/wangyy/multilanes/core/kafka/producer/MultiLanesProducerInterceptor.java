package com.wangyy.multilanes.core.kafka.producer;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
  在生产端给消息增加消息头
 */
@Slf4j
public class MultiLanesProducerInterceptor<K,V> implements ProducerInterceptor {

    @Override
    public ProducerRecord onSend(ProducerRecord record) {
        record.headers().add(FeatureTagContext.NAME, FeatureTagContext.get().getBytes(StandardCharsets.UTF_8));
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }

    public static MultiLanesProducerInterceptor instance() {
        return InstanceHolder.INTERCEPTOR;
    }

    private static class InstanceHolder {
        private static final MultiLanesProducerInterceptor INTERCEPTOR = new MultiLanesProducerInterceptor();
    }
}
