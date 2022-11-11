package com.wangyy.multilanes.core.utils;

import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;

public abstract class FeatureTagUtils {

    public static String buildWithFeatureTag(String str, String featureTag) {
        return str + "_" + featureTag;
    }

    public static boolean needTag() {
        String featureTag = FeatureTagContext.getDEFAULT();
        return featureTag != null && !featureTag.equals(FTConstants.FEATURE_TAG_BASE_LANE_VALUE);
    }
}
