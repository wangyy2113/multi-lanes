package com.wangyy.multilanes.core.infra.finagle.client;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.twitter.finagle.Thrift;
import com.twitter.finagle.context.Contexts;
import com.twitter.util.Duration;
import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.control.zookeeper.MultiLanesZKPathUtils;
import com.wangyy.multilanes.core.infra.LanesInfra;
import com.wangyy.multilanes.core.infra.finagle.MLFinagleFeatureTagContext;
import com.wangyy.multilanes.core.infra.finagle.MLFinagleFeatureTagContext$;
import com.wangyy.multilanes.core.infra.finagle.utils.MultiLanesFinagleUtils;
import com.wangyy.multilanes.core.trace.FTConstants;
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
import scala.runtime.AbstractFunction0;

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

    /**
     * eg:
     * k: feature-x#com.wangyy.multilanes.demo.finagle.api.HelloService
     * v: Thrift.Client.newIface
     */
    private static final Map<String, Object> MULTI_LANES_INTERFACE_2_CLIENT = new ConcurrentHashMap<>();

    private static final ThreadLocal<Boolean> PROXY_FLAG = new ThreadLocal<>();

    //用于判断zk是否存在对应子节点
    //k: eg: /multi-lanes/FINAGLE/feature-x/service/test 下是否有子节点
    private static final LoadingCache<String, Boolean> PATH_EXIST_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .refreshAfterWrite(2, TimeUnit.SECONDS)
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
        Boolean isProxy = PROXY_FLAG.get();
        //不拦截 proxy请求，否则会出现死循环
        if (isProxy != null && isProxy) {
            return callable.call();
        }

        String interfaceName = MultiLanesFinagleUtils.convertFinagleInterfaceName(method.getDeclaringClass().getName());

        Info info = INTERFACE_MAP.get(interfaceName);
        String featureTag = FeatureTagContext.get();
        String multiLanesInterfaceKey;
        String multiLanesPath;

        if (PATH_EXIST_CACHE.get(info.getMultiLanesZkPathSuffix(featureTag))) {
            //TODO 目前这里可以跨泳道 eg:feat-a -> feat-b，也可以限制一下当前是base泳道才能路由所有泳道，否则最多只能路由到自身feat泳道
            multiLanesInterfaceKey = buildMultiLanesInterfaceKey(featureTag, interfaceName);
            multiLanesPath = info.getMultiLanesZkPath(featureTag);
        } else {
            //TODO 这里再考虑clean一下 MULTI_LANES_INTERFACE_2_CLIENT ？ 或者有一个线程定时clean MULTI_LANES_INTERFACE_2_CLIENT
            //不存在对应泳道服务，则路由至base泳道
            multiLanesInterfaceKey = buildMultiLanesInterfaceKey(FTConstants.FEATURE_TAG_BASE_LANE_VALUE, interfaceName);
            multiLanesPath = info.getMultiLanesZkPath(FTConstants.FEATURE_TAG_BASE_LANE_VALUE);
        }
        //路由调用特定Thrift.Client接口
        Object multiLanesClient = MULTI_LANES_INTERFACE_2_CLIENT.computeIfAbsent(multiLanesInterfaceKey, k -> {
            //eg: zk!zkIp:zkPorts!/multi-lanes/FINAGLE/feature-x/service/test
            log.info("[multi-lanes Finagle] proxy multiLanesPath:{}", multiLanesPath);
            return proxyThriftClientIface(multiLanesPath, info.getIface());
        });

        //finagle 使用 Contexts.broadcast() 进行跨进程传值
        return Contexts.broadcast().let(MLFinagleFeatureTagContext$.MODULE$, new MLFinagleFeatureTagContext(FeatureTagContext.get()), new AbstractFunction0<Object>() {
            @Override
            public Object apply() {
                try {
                    PROXY_FLAG.set(true);
                    return method.invoke(multiLanesClient, args);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    PROXY_FLAG.remove();
                }
            }
        });
    }

    private static String buildMultiLanesInterfaceKey(String featureTag, String interfaceName) {
        return featureTag + "#" + interfaceName;
    }

    static void register(String finagleZkAddr, Class iface) {
        String finagleInterfaceName = MultiLanesFinagleUtils.convertFinagleInterfaceName(iface.getName());
        Info info = new Info(finagleZkAddr, iface);
        INTERFACE_MAP.put(finagleInterfaceName, info);
        log.info("[multi-lanes Finagle] record interfaceName:{} info:{}", finagleInterfaceName, info);
    }

    private static Object proxyThriftClientIface(String multiLanesPath, Class iface) {
        //TODO 统一创建入口，才可以保持proxy-client与原client其他逻辑统一
        return Thrift.client()
                .withProtocolFactory(new TBinaryProtocol.Factory())
                .withSessionPool().minSize(10)
                .withSessionPool().maxSize(50)
                .withSessionPool().maxWaiters(100)
                .withSessionQualifier().noFailFast()
                .withRequestTimeout(Duration.fromSeconds(10))
                .newIface(multiLanesPath, MultiLanesFinagleUtils.PROXY_CLIENT_LABEL, iface);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.curatorFramework = applicationContext.getBean(CuratorFramework.class);
    }

    @Data
    static class Info {
        //eg: zk!zkIp:zkPorts!
        private String originFinagleZkAddrPrefix;
        //eg: /service/test
        private String originFinagleZkAddrSuffix;
        private Class iface;

        Info(String baseFinagleZkAddr, Class iface) {
            this.iface = iface;
            String arr[] = baseFinagleZkAddr.split("!/");
            if (arr.length != 2) {
                throw new IllegalArgumentException("illegal baseFinagleZkAddr:" + baseFinagleZkAddr);
            }
            this.originFinagleZkAddrPrefix = arr[0] + '!';
            this.originFinagleZkAddrSuffix = '/' + arr[1];
        }

        //return eg: /multi-lanes/FINAGLE/feature-x/service/test
        String getMultiLanesZkPathSuffix(String featureTag) {
            String suffixWithFeatureTag = MultiLanesFinagleUtils.buildZKPathSuffixWithFeatureTag(featureTag, originFinagleZkAddrSuffix);
            return MultiLanesZKPathUtils.buildPath(LanesInfra.FINAGLE, suffixWithFeatureTag);
        }

        String getMultiLanesZkPath(String featureTag) {
            return originFinagleZkAddrPrefix + getMultiLanesZkPathSuffix(featureTag);
        }
    }
}
