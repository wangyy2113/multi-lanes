package com.wangyy.multilanes.demo.kafka.commons;

import com.typesafe.config.ConfigFactory;

/**
 * Created by houyantao on 2023/1/4
 */
public class KafkaConstants {

    public static final String BROKERS = ConfigFactory.load().getString("kafka.brokers");
    public static final String TOPIC_A = "kafka-test-A";
    public static final String TOPIC_B = "kafka-test-B";
}
