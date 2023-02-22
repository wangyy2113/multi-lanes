package com.wangyy.multilanes.core.kafka.node;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.control.Lanes;
import com.wangyy.multilanes.core.control.zookeeper.MultiLanesNodeWatcher;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Service;

import java.util.UUID;

@ConditionalOnConfig("multi-lanes.enable")
@Service
@Slf4j
public class KafkaNodeWatcher extends MultiLanesNodeWatcher {

    public KafkaNodeWatcher(CuratorFramework curatorFramework) {
        super(curatorFramework);

        String serviceName = getServiceName();
        registerNode(serviceName + "/" + FeatureTagContext.getDEFAULT());
    }

    private String getServiceName() {
        Config config = ConfigFactory.load();
        String path = "spring.application.name";
        if (config.hasPath(path)){
            return config.getString(path);
        }
        String serviceName = "MultiLanes-Service-" + UUID.randomUUID().toString();
        log.info("[multi-lanes Kafka] generate random serviceName: {}", serviceName);
        return serviceName;
    }

    @Override
    protected Lanes lanes() {
        return Lanes.KAFKA;
    }
}
