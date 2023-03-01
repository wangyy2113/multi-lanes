package com.wangyy.multilanes.core.infra.finagle.node;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.control.zookeeper.MultiLanesNodeWatcher;
import com.wangyy.multilanes.core.infra.LanesInfra;
import com.wangyy.multilanes.core.infra.finagle.server.FinagleServerInitInterceptor;
import com.wangyy.multilanes.core.infra.finagle.utils.MultiLanesFinagleUtils;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@ConditionalOnConfig("multi-lanes.enable")
@Service
@Slf4j
public class FinagleNodeWatcher extends MultiLanesNodeWatcher {

    private ScheduledExecutorService scheduler;

    private static final Set<String> RECORD_ZK_PATH_SET = new HashSet<>();

    private static final Map<String, PathChildrenCache> zkNodeMap = new ConcurrentHashMap<>();

    public FinagleNodeWatcher(CuratorFramework curatorFramework) {
        super(curatorFramework);

        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "multi-lanes-finagle-zk-sync-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(() -> {
            Set<String> newRecordSet = new HashSet<>();
            try {
                Set<String> set = new HashSet<>(FinagleServerInitInterceptor.getZK_PATH_SET());
                if (!RECORD_ZK_PATH_SET.containsAll(set)) {
                    //to be add
                    set.removeAll(RECORD_ZK_PATH_SET);
                    //watch zk path
                    for (String path : set) {
                        watchZkPath(path);
                        newRecordSet.add(path);
                    }
                }
            } catch (Exception e) {
                log.error("multi-lanes-finagle-zk-sync-scheduler Exception", e);
            } finally {
                RECORD_ZK_PATH_SET.addAll(newRecordSet);
            }
        }, 1000, 2000, TimeUnit.MILLISECONDS);
    }


    private void watchZkPath(String path) throws Exception {
        PathChildrenCache nodesCache = zkNodeMap.computeIfAbsent(path, p -> new PathChildrenCache(curatorFramework, p, true));
        nodesCache.start();
        nodesCache.getListenable().addListener((client, event) -> {
            if (event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)) {
                log.info("[multi-lanes Finagle] zk PathChildrenCacheEvent INITIALIZED. event: {}", event.toString());
                for (ChildData node : nodesCache.getCurrentData()) {
                    //在multi-lanes path注册相同nodes
                    //若path = /service/test 则 multi-lanes path = /multi-lanes/FINAGLE/{featureTag}/service/test
                    registerNode(buildSuffixWithFeatureTag(node.getPath()), node.getData());
                }
            } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                ChildData node = event.getData();
                log.info("[multi-lanes Finagle] zk PathChildrenCacheEvent CHILD_ADDED. node: {}", node.toString());
                registerNode(buildSuffixWithFeatureTag(node.getPath()), node.getData());
            } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                ChildData node = event.getData();
                log.info("[multi-lanes Finagle] zk PathChildrenCacheEvent CHILD_REMOVED. node: {}", node.toString());
                deleteNode(buildSuffixWithFeatureTag(node.getPath()));
            }
        });
    }

    private String buildSuffixWithFeatureTag(String path) {
        return MultiLanesFinagleUtils.buildZKPathSuffixWithFeatureTag(FeatureTagContext.getDEFAULT(), path);
    }

    @Override
    protected LanesInfra lanesInfra() {
        return LanesInfra.FINAGLE;
    }

}
