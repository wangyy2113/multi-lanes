package com.wangyy.multilanes.core.infra.finagle.node;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.control.zookeeper.MultiLanesNodeWatcher;
import com.wangyy.multilanes.core.infra.LanesInfra;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Service;


@ConditionalOnConfig("multi-lanes.enable")
@Service
@Slf4j
public class FinagleNodeWatcher extends MultiLanesNodeWatcher {

    public FinagleNodeWatcher(CuratorFramework curatorFramework) {
        super(curatorFramework);
    }

    @Override
    protected LanesInfra lanesInfra() {
        return LanesInfra.FINAGLE;
    }

}
