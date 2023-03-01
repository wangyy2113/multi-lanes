package com.wangyy.multilanes.core.infra.finagle.server;

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
 * Server初始化会调用 ListeningServer#announce，拦截入参适配泳道逻辑
 *
 */
@Slf4j
@ConditionalOnConfig("multi-lanes.enable")
@Configuration
class FinagleServerByteBuddyAgent implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ByteBuddyAgent.install();
        new AgentBuilder
                .Default()
                .type(ElementMatchers.nameStartsWith("com.twitter.finagle.ListeningServer"))
                .transform((builder, typeDescription, classLoader, javaModule) ->
                        builder.method(ElementMatchers.hasMethodName("announce").and(ElementMatchers.isPublic()))
                                .intercept(MethodDelegation.to(FinagleServerInitInterceptor.class)))
                .installOnByteBuddyAgent();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
