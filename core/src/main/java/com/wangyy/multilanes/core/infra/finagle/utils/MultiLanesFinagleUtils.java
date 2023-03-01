package com.wangyy.multilanes.core.infra.finagle.utils;

public class MultiLanesFinagleUtils {

    public static final String PROXY_CLIENT_LABEL = "[multi-lanes_proxy-label]";


    public static boolean checkFinagleZkAddrValid(String finagleZkAddr) {
        if (finagleZkAddr == null || finagleZkAddr.isEmpty()) {
            return false;
        }
        //eg: zk!zkIp:zkPorts!/service/test
        return finagleZkAddr.startsWith("zk!") && finagleZkAddr.contains("/");
    }

    public static String convertZkPath(String finagleZkAddr) {
        if (!checkFinagleZkAddrValid(finagleZkAddr)) {
            throw new IllegalArgumentException("unsupported finagleZkAddr:" + finagleZkAddr);
        }
        String[] arr = finagleZkAddr.split("!");
        //eg: 取 zk!127.0.0.1:2181!/service/test!0 中的 /service/test
        return arr[2];
    }

    public static String convertFinagleInterfaceName(String name) {
        if (name == null || name.isEmpty() || !name.contains("$")) {
            throw new IllegalArgumentException("illegal name:" + name);
        }
        return name.split("\\$")[0];
    }


    public static String buildZKPathSuffixWithFeatureTag(String featureTag, String path) {
        return featureTag + path;
    }


}
