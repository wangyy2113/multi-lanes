package com.wangyy.multilanes.core.control.zookeeper;

import com.wangyy.multilanes.core.infra.LanesInfra;

public class MultiLanesZKPathUtils {

    private static final String NODE_PREFIX = "/multi-lanes";

    private static final String SPLIT = "/";

    public static String buildPath(LanesInfra lanesInfra, String suffix) {
        return NODE_PREFIX + SPLIT + lanesInfra.name() + SPLIT + suffix;
    }
}
