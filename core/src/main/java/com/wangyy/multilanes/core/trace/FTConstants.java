package com.wangyy.multilanes.core.trace;

public class FTConstants {

    private FTConstants() {}

    public static final String FEATURE_TAG_PATH = "featureTag";

    public static final String FEATURE_TAG_BASE_LANE_VALUE = "main";

    public static String buildWithFeatureTag(String str, String featureTag) {
        return str + "_" + featureTag;
    }

}
