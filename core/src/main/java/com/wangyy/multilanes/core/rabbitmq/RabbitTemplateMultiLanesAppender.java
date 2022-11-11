package com.wangyy.multilanes.core.rabbitmq;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.rabbitmq.postprocessor.RabbitFeatureTagAfterReceiveAppender;
import com.wangyy.multilanes.core.rabbitmq.postprocessor.RabbitFeatureTagBeforePublishAppender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;

/*
 * RabbitTemplate增加消息发送前 & 接收后处理器，用于featureTag打标
 *
 *
 */
@ConditionalOnConfig("multi-lanes.rabbit.enable")
@Slf4j
@Component
public class RabbitTemplateMultiLanesAppender {

    public RabbitTemplateMultiLanesAppender(ApplicationContext applicationContext) {
        //RabbitTemplate
        Collection<RabbitTemplate> rabbitTemplates = applicationContext.getBeansOfType(RabbitTemplate.class).values();
        if (!rabbitTemplates.isEmpty()) {
            rabbitTemplates.forEach(this::init);
        }
    }

    public void init(RabbitTemplate rabbitTemplate) {
        log.info("[multi-lanes] RabbitMQ registerProcessor rabbitTemplate:{}", rabbitTemplate);

        rabbitTemplate.addBeforePublishPostProcessors(RabbitFeatureTagBeforePublishAppender.instance());

        rabbitTemplate.addAfterReceivePostProcessors(RabbitFeatureTagAfterReceiveAppender.instance());
    }

}
