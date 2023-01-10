package com.wangyy.multilanes.core.kafka;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
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
    public KafkaAspect kafkaAspect() {
        return new KafkaAspect();
    }

    @PostConstruct
    public void init() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerZkNode() {

    }

}
