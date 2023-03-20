package com.wangyy.multilanes.core.control.zookeeper;

import com.typesafe.config.ConfigFactory;
import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnConfig("spring.cloud.zookeeper.enabled")
@Slf4j
public class ZkAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = CuratorFramework.class)
    public CuratorFramework curatorFramework() throws InterruptedException {
        int blockUntilConnectedWait = 5;

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        builder.retryPolicy(new ExponentialBackoffRetry(50, 5, 500));
        builder.connectString(ConfigFactory.load().getString("spring.cloud.zookeeper.connectString"));
        CuratorFramework curator = builder.build();

        curator.start();
        log.info("blocking until connected to zookeeper for " + blockUntilConnectedWait + TimeUnit.SECONDS);
        curator.blockUntilConnected(blockUntilConnectedWait, TimeUnit.SECONDS);
        log.info("connected to zookeeper");

        return curator;
    }
}
