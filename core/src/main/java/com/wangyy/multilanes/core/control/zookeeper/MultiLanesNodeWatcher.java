package com.wangyy.multilanes.core.control.zookeeper;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.wangyy.multilanes.core.infra.LanesInfra;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by houyantao on 2023/2/20
 */
@Slf4j
public abstract class MultiLanesNodeWatcher {

    private static final String NODE_PREFIX = "/multi-lanes";

    private CuratorFramework curatorFramework;

    private final LoadingCache<String, Boolean> nodeCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .refreshAfterWrite(1, TimeUnit.SECONDS)
            .build(path -> curatorFramework.checkExists().forPath(path) != null);

    protected MultiLanesNodeWatcher(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    protected abstract LanesInfra lanesInfra();

    public void registerNode(String suffix) {
        if (StringUtils.isEmpty(suffix)) {
            throw new IllegalArgumentException("suffix cannot be empty!");
        }
        String path = buildPath(suffix);
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

    public boolean exist(String suffix) {
        return nodeCache.get(buildPath(suffix));
    }

    private String buildPath(String suffix) {
        return NODE_PREFIX + "/" + lanesInfra().name() + "/" + suffix;
    }
}
