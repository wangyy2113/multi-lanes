package com.wangyy.multilanes.core.trace;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;
import lombok.NonNull;

/*
 *
 * 流量的featureTag, 每一个请求都有自己的featureTag
 * 默认取配置文件中的${featureTag}, 若无相关配置则默认main
 *
 */
public class FeatureTagContext {

    private FeatureTagContext() {}

    public static final String NAME = "featureTag";

    private static final TransmittableThreadLocal<String> FEATURE_TAG = new TransmittableThreadLocal<>();

    //默认取配置文件中的${featureTag}, 若无相关配置则默认main
    @Getter
    private static final String DEFAULT;

    static {
        Config config = ConfigFactory.load();
        DEFAULT = config.hasPath(FTConstants.FEATURE_TAG_PATH) ?
                config.getString(FTConstants.FEATURE_TAG_PATH) : FTConstants.FEATURE_TAG_BASE_LANE_VALUE;
    }

    public static String get() {
        String ft = FEATURE_TAG.get();
        return ft == null ? DEFAULT : ft;
    }

    public static void set(@NonNull String featureTag) {
        FEATURE_TAG.set(featureTag);
    }

}
