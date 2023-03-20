package com.wangyy.multilanes.core.infra.finagle.client;

import com.wangyy.multilanes.core.infra.finagle.utils.MultiLanesFinagleUtils;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;

@Slf4j
public class FinagleClientInitInterceptor {

    @RuntimeType
    public static Object newIfaceIntercept(@AllArguments Object[] args,
                                           @SuperCall Callable<?> callable) throws Exception {

        Object result = callable.call();
        proxyClient(args);
        return result;
    }

    private static void proxyClient(Object[] args) {
        if (args.length < 2) {
            return;
        }
        //ignore ProxyClient build
        if (Arrays.stream(args).filter(arg -> arg instanceof String).anyMatch(arg -> ((String) arg).equals(MultiLanesFinagleUtils.PROXY_CLIENT_LABEL))) {
            return;
        }
        Optional<Object> addrOpt = Arrays.stream(args)
                .filter(arg -> arg instanceof String)
                .filter(arg -> MultiLanesFinagleUtils.checkFinagleZkAddrValid(((String) arg)))
                .findFirst();

        Optional<Object> ifaceOpt = Arrays.stream(args)
                .filter(arg -> arg instanceof Class)
                .findFirst();

        if (addrOpt.isPresent() && ifaceOpt.isPresent()) {
            //addr = zk!zkIp:zkPorts!/service/test
            //ifaceName = com.wangyy.multilanes.demo.finagle.api.HelloService$FutureIface
            String addr = (String) addrOpt.get();
            Class iface = (Class) ifaceOpt.get();

            FinagleInterfaceInterceptor.register(addr, iface);

        } else {
            log.debug("[multi-lanes Finagle] ignore newIface argsSize:{} args:{}", args.length, args);
        }
    }
}
