package com.wangyy.multilanes.core.kafka;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by houyantao on 2022/12/28
 */
@ConditionalOnConfig("multi-lanes.enable")
@Configuration
@Slf4j
public class KafkaMultiLanesContextInit {

    @Bean
    public KafkaNodeWatcher kafkaNodeWatcher(CuratorFramework curatorFramework) {
        return new KafkaNodeWatcher(curatorFramework);
    }

    @Bean
    public MultiLanesConsumerInterceptor consumerInterceptor(KafkaNodeWatcher nodeWatcher) {
        return new MultiLanesConsumerInterceptor(nodeWatcher);
    }

    @Bean
    public KafkaProducerBeanPostProcessor kafkaProducerBeanPostProcessor() {
        return new KafkaProducerBeanPostProcessor();
    }

    @Bean
    public KafkaConsumerBeanPostProcessor kafkaConsumerBeanPostProcessor(KafkaNodeWatcher kafkaNodeWatcher,
                                                                         ApplicationContext applicationContext,
                                                                         MultiLanesConsumerInterceptor consumerInterceptor) {
        return new KafkaConsumerBeanPostProcessor(kafkaNodeWatcher, applicationContext, consumerInterceptor);
    }

    @Bean
    public KafkaAspect kafkaAspect() {
        return new KafkaAspect();
    }
}
