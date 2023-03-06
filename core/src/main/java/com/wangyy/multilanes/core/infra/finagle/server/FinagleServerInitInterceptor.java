package com.wangyy.multilanes.core.infra.finagle.server;

import com.wangyy.multilanes.core.control.zookeeper.MultiLanesZKPathUtils;
import com.wangyy.multilanes.core.infra.LanesInfra;
import com.wangyy.multilanes.core.infra.finagle.utils.MultiLanesFinagleUtils;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 拦截Server announce初始化入参(rpc server注册host)后记录
 *
 * 暂时只支持finagle server注册zk
 */
@Slf4j
public class FinagleServerInitInterceptor {

    private static final ThreadLocal<Boolean> PROXY_FLAG = new ThreadLocal<>();

    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @This Object thisObj,
                                   @SuperCall Callable<?> callable,
                                   @AllArguments Object[] args) throws Exception {
        boolean isProxy = PROXY_FLAG.get() != null && PROXY_FLAG.get();
        //不拦截 proxy请求，否则会出现死循环
        if (isProxy) {
            return callable.call();
        }

        if (args == null || args.length < 2) {
            throw new UnsupportedOperationException("unsupported method:" + method.getName() + " parameters:" + method.getParameters());
        }

        String finagleZkAddr = (String) args[1];
        if (!MultiLanesFinagleUtils.checkFinagleZkAddrValid(finagleZkAddr)) {
            throw new UnsupportedOperationException("unsupported finagleZkAddr:" + finagleZkAddr);
        }

        String multiLanesZkAddr = MultiLanesFinagleUtils.convertZkPathToMultiLanes(finagleZkAddr, MultiLanesZKPathUtils.buildPath(LanesInfra.FINAGLE, FeatureTagContext.getDEFAULT()));
        log.info("[multi-lanes Finagle] change zkAddr before:{} after:{}", finagleZkAddr, multiLanesZkAddr);

        args[1] = multiLanesZkAddr;

/*        if (FeatureTagContext.isBaseLine() && !isProxy) {
            //base-line保留原有zk节点 加原有节点close逻辑?
            callable.call();
        }*/

        try {
            PROXY_FLAG.set(true);
            return method.invoke(thisObj, args);
        } finally {
            PROXY_FLAG.remove();
        }
    }
}
