package com.wangyy.multilanes.core.kafka;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.TimeUnit;

/**
 * todo 和 rabbitNodeWatch 整合
 * Created by houyantao on 2023/1/6
 */
public class KafkaNodeWatcher {

    private CuratorFramework curatorFramework;

    public KafkaNodeWatcher(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    private final LoadingCache<String, Boolean> nodeCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .refreshAfterWrite(1, TimeUnit.SECONDS)
            .build(path -> curatorFramework.checkExists().forPath(path) != null);

    public void registerZKPath(String path) {
        try {
            curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isExist(String path) {
        return nodeCache.get(path);
    }

    public static String path(String topic, String group) {
        return "/" + topic + "/" + group;
    }
}
