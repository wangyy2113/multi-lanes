package com.wangyy.multilanes.core.infra.finagle.client;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.twitter.finagle.Thrift;
import com.twitter.util.Duration;
import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.control.zookeeper.MultiLanesZKPathUtils;
import com.wangyy.multilanes.core.infra.LanesInfra;
import com.wangyy.multilanes.core.infra.finagle.utils.MultiLanesFinagleUtils;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.*;
import org.apache.curator.framework.CuratorFramework;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ConditionalOnConfig("multi-lanes.enable")
@Slf4j
@Component
public class FinagleInterfaceInterceptor implements ApplicationContextAware {

    private static CuratorFramework curatorFramework;

    /**
     * eg:
     * k: com.wangyy.multilanes.demo.finagle.api.HelloService
     */
    private static final Map<String, Info> INTERFACE_MAP = new ConcurrentHashMap<>();

    private static final Map<String, Object> MULTI_LANES_INTERFACE_2_CLIENT = new ConcurrentHashMap<>();

    private static final ThreadLocal<Boolean> PROXY_FLAG = new ThreadLocal<>();

    //用于判断zk是否存在对应节点
    //k: eg: /multi-lanes/FINAGLE/feature-x/service/test
    private static final LoadingCache<String, Boolean> PATH_EXIST_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .refreshAfterWrite(1, TimeUnit.SECONDS)
            .build(path -> {
                try {
                    List<String> childs = curatorFramework.getChildren().forPath(path);
                    return !CollectionUtils.isEmpty(childs);
                } catch (KeeperException.NoNodeException e) {
                    return false;
                }
            });

    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @SuperCall Callable<?> callable,
                                   @AllArguments Object[] args) throws Exception {
        boolean isProxy = PROXY_FLAG.get() != null && PROXY_FLAG.get();
        //不拦截 proxy请求，否则会出现死循环
        if (isProxy) {
            return callable.call();
        }
        String interfaceName = MultiLanesFinagleUtils.convertFinagleInterfaceName(method.getDeclaringClass().getName());

        Info info = INTERFACE_MAP.get(interfaceName);
        //不存在对应泳道服务，则路由至base泳道(原生调用即可)
        if (!PATH_EXIST_CACHE.get(info.getMultiLanesZkPathSuffix())) {
            return callable.call();
        }

        //路由调用特定Thrift.Client接口
        String multiLanesInterfaceKey = buildMultiLanesInterfaceKey(interfaceName);
        Object multiLanesClient = MULTI_LANES_INTERFACE_2_CLIENT.computeIfAbsent(multiLanesInterfaceKey, k -> {
            //eg: zk!zkIp:zkPorts!/multi-lanes/FINAGLE/feature-x/service/test
            String multiLanesPath = info.getMultiLanesZkPath();
            log.info("[multi-lanes Finagle] proxy multiLanesPath:{}", multiLanesPath);

            //TODO 统一创建入口
            return Thrift.client()
                    .withProtocolFactory(new TBinaryProtocol.Factory())
                    .withSessionPool().minSize(10)
                    .withSessionPool().maxSize(50)
                    .withSessionPool().maxWaiters(100)
                    .withSessionQualifier().noFailFast()
                    .withRequestTimeout(Duration.fromSeconds(10))
                    .newIface(multiLanesPath, MultiLanesFinagleUtils.PROXY_CLIENT_LABEL, info.getIface());
        });
        try {
            PROXY_FLAG.set(true);
            return method.invoke(multiLanesClient, args);
        } finally {
            PROXY_FLAG.set(false);
        }
    }

    private static String buildMultiLanesInterfaceKey(String interfaceName) {
        return FeatureTagContext.get() + "#" + interfaceName;
    }

    static void register(String finagleZkAddr, Class iface) {
        String finagleInterfaceName = MultiLanesFinagleUtils.convertFinagleInterfaceName(iface.getName());
        Info info = new Info(finagleZkAddr, iface);
        INTERFACE_MAP.put(finagleInterfaceName, info);
        log.info("[multi-lanes Finagle] record interfaceName:{} info:{}", finagleInterfaceName, info);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.curatorFramework = applicationContext.getBean(CuratorFramework.class);
    }

    @Data
    static class Info {
        //eg: zk!zkIp:zkPorts!
        private String baseFinagleZkAddrPrefix;
        //eg: /service/test
        private String baseFinagleZkAddrSuffix;
        private Class iface;

        Info(String baseFinagleZkAddr, Class iface) {
            this.iface = iface;
            String arr[] = baseFinagleZkAddr.split("!/");
            if (arr.length != 2) {
                throw new IllegalArgumentException("illegal baseFinagleZkAddr:" + baseFinagleZkAddr);
            }
            this.baseFinagleZkAddrPrefix = arr[0] + '!';
            this.baseFinagleZkAddrSuffix = '/' + arr[1];
        }

        String getMultiLanesZkPathSuffix() {
            String suffixWithFeatureTag = MultiLanesFinagleUtils.buildZKPathSuffixWithFeatureTag(FeatureTagContext.get(), baseFinagleZkAddrSuffix);
            return MultiLanesZKPathUtils.buildPath(LanesInfra.FINAGLE, suffixWithFeatureTag);
        }

        String getMultiLanesZkPath() {
            return baseFinagleZkAddrPrefix + getMultiLanesZkPathSuffix();
        }
    }
}
