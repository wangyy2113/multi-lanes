package com.wangyy.multilanes.core.kafka;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.kafka.consumer.KafkaConsumerAspect;
import com.wangyy.multilanes.core.kafka.consumer.KafkaConsumerGroupChangeProcessor;
import com.wangyy.multilanes.core.kafka.node.KafkaNodeWatcher;
import com.wangyy.multilanes.core.kafka.producer.KafkaProducerAspect;
import com.wangyy.multilanes.core.kafka.producer.KafkaProducerFeatureTagProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Created by houyantao on 2022/12/28
 */
@ConditionalOnConfig("multi-lanes.enable")
@Configuration
@Slf4j
public class KafkaMultiLanesContextInit {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public KafkaProducerAspect kafkaProducerAspect() {
        return new KafkaProducerAspect();
    }

    @Bean
    public KafkaConsumerAspect kafkaConsumerAspect(KafkaNodeWatcher nodeWatcher) {
        return new KafkaConsumerAspect(nodeWatcher);
    }

    @PostConstruct
    public void init() {
        new KafkaProducerFeatureTagProcessor(applicationContext).lance();
        new KafkaConsumerGroupChangeProcessor(applicationContext).lance();
    }
}
