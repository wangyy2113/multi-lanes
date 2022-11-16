package com.wangyy.multilanes.core.rabbitmq;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.rabbitmq.postprocessor.RabbitFeatureTagAfterReceiveAppender;
import com.wangyy.multilanes.core.rabbitmq.postprocessor.RabbitTemplateFeatureTagBeforePublishAppender;
import com.wangyy.multilanes.core.rabbitmq.service.RabbitNodeWatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/*
 * RabbitTemplate增加消息发送前 & 接收后处理器，用于featureTag打标
 * exchange信息注册到zookeeper
 *
 *
 */
@ConditionalOnConfig("multi-lanes.enable")
@Slf4j
@Component
public class RabbitMultiLanesContextsIni {

    public RabbitMultiLanesContextsIni(ApplicationContext applicationContext) {

        rabbitTemplatesInit(applicationContext);
        listenerPostProcessorInit(applicationContext);

        exchangeRegister(applicationContext);
    }


    private void rabbitTemplatesInit(ApplicationContext applicationContext) {
        Collection<RabbitTemplate> rabbitTemplates = applicationContext.getBeansOfType(RabbitTemplate.class).values();
        if (!rabbitTemplates.isEmpty()) {
            rabbitTemplates.forEach(rt -> {
                log.info("[multi-lanes] RabbitMQ registerProcessor rabbitTemplate:{}", rt);

                rt.addBeforePublishPostProcessors(RabbitTemplateFeatureTagBeforePublishAppender.instance());

                rt.addAfterReceivePostProcessors(RabbitFeatureTagAfterReceiveAppender.instance());
            });
        }
    }

    private void exchangeRegister(ApplicationContext applicationContext) {
        RabbitNodeWatcher rabbitNodeWatcher = applicationContext.getBean(RabbitNodeWatcher.class);
        List<Exchange> exchanges = new LinkedList(applicationContext.getBeansOfType(Exchange.class).values());

        exchanges.forEach(rabbitNodeWatcher::registerExchange);
    }

    private void listenerPostProcessorInit(ApplicationContext applicationContext) {
        SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory = applicationContext.getBean(SimpleRabbitListenerContainerFactory.class);
        simpleRabbitListenerContainerFactory.setAfterReceivePostProcessors(RabbitFeatureTagAfterReceiveAppender.instance());
    }

}
