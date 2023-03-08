package com.wangyy.multilanes.core.infra.finagle.server;

import com.twitter.finagle.context.Contexts;
import com.wangyy.multilanes.core.infra.finagle.MLFinagleFeatureTagContext;
import com.wangyy.multilanes.core.infra.finagle.MLFinagleFeatureTagContext$;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.*;

import java.util.concurrent.Callable;

/**
 * 拦截Server service请求处理接口
 * 从Contexts.broadcast()中取出featureTag并set FeatureTagContext
 */
@Slf4j
public class FinagleServerHandleInterceptor {

    @RuntimeType
    public static Object serviceIntercept(@SuperCall Callable<?> callable) throws Exception {
        if (Contexts.broadcast().contains(MLFinagleFeatureTagContext$.MODULE$)) {
            MLFinagleFeatureTagContext ft = Contexts.broadcast().apply(MLFinagleFeatureTagContext$.MODULE$);
            FeatureTagContext.set(ft.featureTag());
        }
        return callable.call();
    }
}
