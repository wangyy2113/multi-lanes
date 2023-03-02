package com.wangyy.multilanes.core.infra.finagle.client;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * 拦截增强Finagle Client初始化逻辑
 *
 * Client初始化会调用 ThriftRichClient#newIface，拦截入参以便做动态代理
 *
 */
@Slf4j
@ConditionalOnConfig("multi-lanes.enable")
@Configuration
class FinagleClientByteBuddyAgent implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ByteBuddyAgent.install();
        new AgentBuilder
                .Default()
                //拦截初始化接口
                .type(ElementMatchers.nameStartsWith("com.twitter.finagle.ThriftRichClient"))
                .transform((builder, typeDescription, classLoader, javaModule) ->
                        builder.method(ElementMatchers.hasMethodName("newIface").and(ElementMatchers.isPublic()))
                                .intercept(MethodDelegation.to(FinagleClientInitInterceptor.class)))
                //代理拦截rpc接口
                .type(ElementMatchers.nameEndsWith("$FinagleClient"))
                .transform((builder, typeDescription, classLoader, javaModule) ->
                        builder.method(ElementMatchers.isPublic())
                        .intercept(MethodDelegation.to(FinagleInterfaceInterceptor.class)))
                .installOnByteBuddyAgent();
        log.info("install");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
