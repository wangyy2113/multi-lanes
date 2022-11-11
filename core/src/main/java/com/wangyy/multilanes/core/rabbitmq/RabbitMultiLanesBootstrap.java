package com.wangyy.multilanes.core.rabbitmq;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.rabbitmq.init.RabbitAnnotationListenerMultiLanesIni;
import com.wangyy.multilanes.core.rabbitmq.init.RabbitDeclarableMultiLanesIni;
import com.wangyy.multilanes.core.rabbitmq.init.RabbitListenerContainerMultiLanesIni;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;


/*
 * RabbitMQ 泳道资源初始化引导类
 *
 */
@ConditionalOnConfig("multi-lanes.rabbit.enable")
@Slf4j
@Component
public class RabbitMultiLanesBootstrap implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        new RabbitAnnotationListenerMultiLanesIni(registry).init();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //Exchange Queue Binding
        new RabbitDeclarableMultiLanesIni(beanFactory).init();

        new RabbitListenerContainerMultiLanesIni(beanFactory).init();
    }
}
