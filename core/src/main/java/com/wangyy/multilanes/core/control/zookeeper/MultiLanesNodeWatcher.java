package com.wangyy.multilanes.core.control.zookeeper;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by houyantao on 2023/2/20
 */
@Slf4j
@Service
@ConditionalOnConfig("multi-lanes.enable")
public class MultiLanesNodeWatcher {

    private static final String NODE_PREFIX = "/multi-lanes";

    @Autowired
    private CuratorFramework curatorFramework;

    @Value("${spring.application.name}")
    private String serviceName;

    private final LoadingCache<String, Boolean> nodeCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .refreshAfterWrite(1, TimeUnit.SECONDS)
            .build(path -> curatorFramework.checkExists().forPath(path) != null);

    @PostConstruct
    public void init() {
        registerServicePath();
    }

    public void registerServicePath() {
        String path = buildPath(FeatureTagContext.getDEFAULT());
        try {
            curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path);
        } catch (Exception e) {
            log.error("register path {} to zookeeper error", path, e);
            throw new RuntimeException(e);
        }
    }

    public boolean exist(String featureTag) {
        return nodeCache.get(buildPath(featureTag));
    }

    private String buildPath(String featureTag) {
        return NODE_PREFIX + "/" + serviceName + "/" + featureTag;
    }
}
