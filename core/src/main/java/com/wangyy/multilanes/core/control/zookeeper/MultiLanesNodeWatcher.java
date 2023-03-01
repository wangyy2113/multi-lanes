package com.wangyy.multilanes.core.control.zookeeper;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.wangyy.multilanes.core.infra.LanesInfra;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.zookeeper.CreateMode;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by houyantao on 2023/2/20
 */
@Slf4j
public abstract class MultiLanesNodeWatcher {

    protected CuratorFramework curatorFramework;

    private final LoadingCache<String, Boolean> nodeCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .refreshAfterWrite(1, TimeUnit.SECONDS)
            .build(path -> curatorFramework.checkExists().forPath(path) != null);

    protected MultiLanesNodeWatcher(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    protected abstract LanesInfra lanesInfra();

    public void registerNode(String suffix, byte[] datas) {
        if (StringUtils.isEmpty(suffix)) {
            throw new IllegalArgumentException("suffix cannot be empty!");
        }
        String path =  MultiLanesZKPathUtils.buildPath(lanesInfra(), suffix);
        try {
            ACLBackgroundPathAndBytesable<String> aclBackgroundPathAndBytesable = curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL);
            if (datas == null) {
                aclBackgroundPathAndBytesable.forPath(path);
            } else {
                aclBackgroundPathAndBytesable.forPath(path, datas);
            }
        } catch (Exception e) {
            log.error("register path {} to zookeeper error. datas:{}", path, datas, e);
            throw new RuntimeException(e);
        }
    }

    public void registerNode(String suffix) {
        registerNode(suffix, null);
    }

    public void deleteNode(String suffix) {
        if (StringUtils.isEmpty(suffix)) {
            throw new IllegalArgumentException("suffix cannot be empty!");
        }
        String path = MultiLanesZKPathUtils.buildPath(lanesInfra(), suffix);
        try {
            curatorFramework.delete()
                    .deletingChildrenIfNeeded()
                    .forPath(path);
        } catch (Exception e) {
            log.error("delete zookeeper path {} error ", path, e);
            throw new RuntimeException(e);
        }
    }

    public boolean exist(String suffix) {
        return nodeCache.get(MultiLanesZKPathUtils.buildPath(lanesInfra(), suffix));
    }

}
