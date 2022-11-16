package com.wangyy.multilanes.core.rabbitmq.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.amqp.core.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/*
 * Rabbit Exchange节点信息存储到zk
 * Exchange节点信息用于exchange路由
 *
 */
@Component
public class RabbitNodeWatcher {

    private static final Config CONFIG = ConfigFactory.load();

    @Autowired
    private CuratorFramework curatorFramework;

    private final LoadingCache<String, Boolean> nodeCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .refreshAfterWrite(1, TimeUnit.SECONDS)
            .build(exchange -> curatorFramework.checkExists().forPath(getRabbitNodePath(exchange)) != null);


    public boolean isExchangeExist(String exchange) {
        return nodeCache.get(exchange);
    }

    public void registerExchange(Exchange exchange) {
        try {
            curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(getRabbitNodePath(exchange.getName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getRabbitNodePath(String exchange) {
        return CONFIG.getString("multi-lanes.rabbit.node.prefix") + exchange;
    }
}
