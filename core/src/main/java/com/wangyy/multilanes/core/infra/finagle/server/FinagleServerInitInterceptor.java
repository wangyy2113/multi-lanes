package com.wangyy.multilanes.core.infra.finagle.server;

import com.wangyy.multilanes.core.infra.finagle.utils.MultiLanesFinagleUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 拦截Server announce初始化入参(rpc server注册host)后记录
 *
 * 暂时只支持finagle server注册zk
 */
@Slf4j
public class FinagleServerInitInterceptor {

    @Getter
    private static final Set<String> ZK_PATH_SET = ConcurrentHashMap.newKeySet();

    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @SuperCall Callable<?> callable,
                                   @AllArguments Object[] args) throws Exception {

        log.info("[multi-lanes Finagle] intercept methodName:{} args:{}", method.getName(), args);
        // 执行原函数
        Object result = callable.call();
        //记录
        String zkPath = MultiLanesFinagleUtils.convertZkPath((String) args[1]);
        ZK_PATH_SET.add(zkPath);
        log.info("[multi-lanes Finagle] intercept add zkPath:{}", zkPath);
        return result;
    }
}
