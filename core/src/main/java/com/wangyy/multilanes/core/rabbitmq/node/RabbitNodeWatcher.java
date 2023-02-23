package com.wangyy.multilanes.core.rabbitmq.node;

import com.wangyy.multilanes.core.control.LanesInfra;
import com.wangyy.multilanes.core.control.zookeeper.MultiLanesNodeWatcher;
import org.apache.curator.framework.CuratorFramework;

/*
 * Rabbit Exchange节点信息存储到zk
 * Exchange节点信息用于exchange路由
 * 默认实现
 *
 */
public class RabbitNodeWatcher extends MultiLanesNodeWatcher {

    public RabbitNodeWatcher(CuratorFramework curatorFramework) {
        super(curatorFramework);
    }

    @Override
    protected LanesInfra lanesInfra() {
        return LanesInfra.RABBITMQ;
    }
}
