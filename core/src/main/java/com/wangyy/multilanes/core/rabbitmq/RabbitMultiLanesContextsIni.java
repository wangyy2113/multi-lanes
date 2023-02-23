package com.wangyy.multilanes.core.rabbitmq;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.rabbitmq.postprocessor.RabbitFeatureTagAfterReceiveAppender;
import com.wangyy.multilanes.core.rabbitmq.postprocessor.RabbitTemplateFeatureTagBeforePublishAppender;
import com.wangyy.multilanes.core.rabbitmq.node.RabbitNodeWatcher;
import com.wangyy.multilanes.core.rabbitmq.solver.RabbitProducerMultiLanesAspect;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/*
 * RabbitTemplate增加消息发送前 & 接收后处理器，用于featureTag打标
 * exchange信息注册到RabbitNodeWatcher
 * RabbitProducerMultiLanesAspect用于rabbitmq流量路由
 *
 *
 */
@ConditionalOnConfig("multi-lanes.enable")
@Slf4j
@Configuration
public class RabbitMultiLanesContextsIni {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public RabbitNodeWatcher rabbitNodeWatcher(CuratorFramework curatorFramework) {
        return new RabbitNodeWatcher(curatorFramework);
    }

    @Bean
    public RabbitProducerMultiLanesAspect rabbitProducerMultiLanesAspect(RabbitNodeWatcher rabbitNodeWatcher) {
        return new RabbitProducerMultiLanesAspect(rabbitNodeWatcher);
    }

    @PostConstruct
    public void init() {
        rabbitTemplatesInit();
        listenerPostProcessorInit();
        exchangeRegister();
    }


    private void rabbitTemplatesInit() {
        Collection<RabbitTemplate> rabbitTemplates = applicationContext.getBeansOfType(RabbitTemplate.class).values();
        if (!rabbitTemplates.isEmpty()) {
            rabbitTemplates.forEach(rt -> {
                log.info("[multi-lanes] RabbitMQ registerProcessor rabbitTemplate:{}", rt);

                rt.addBeforePublishPostProcessors(RabbitTemplateFeatureTagBeforePublishAppender.instance());

                rt.addAfterReceivePostProcessors(RabbitFeatureTagAfterReceiveAppender.instance());
            });
        }
    }

    private void exchangeRegister() {
        RabbitNodeWatcher rabbitNodeWatcher = applicationContext.getBean(RabbitNodeWatcher.class);
        List<Exchange> exchanges = new LinkedList(applicationContext.getBeansOfType(Exchange.class).values());

        exchanges.stream().map(Exchange::getName).forEach(rabbitNodeWatcher::registerNode);
    }

    private void listenerPostProcessorInit() {
        SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory = applicationContext.getBean(SimpleRabbitListenerContainerFactory.class);
        simpleRabbitListenerContainerFactory.setAfterReceivePostProcessors(RabbitFeatureTagAfterReceiveAppender.instance());
    }

}
