package com.wangyy.multilanes.core.rabbitmq.init;

import com.wangyy.multilanes.core.rabbitmq.postprocessor.RabbitFeatureTagAfterReceiveAppender;
import com.wangyy.multilanes.core.rabbitmq.postprocessor.RabbitFeatureTagBeforePublishAppender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/*
 * RabbitTemplate增加消息发送前 & 接收后处理器，用于featureTag打标
 *
 *
 */
@Slf4j
public class RabbitTemplateMultiLanesIni {

    private RabbitTemplate rabbitTemplate;

    public RabbitTemplateMultiLanesIni(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void init() {
        log.info("[multi-lanes] RabbitMQ registerProcessor...");

        rabbitTemplate.addBeforePublishPostProcessors(RabbitFeatureTagBeforePublishAppender.instance());

        rabbitTemplate.addAfterReceivePostProcessors(RabbitFeatureTagAfterReceiveAppender.instance());
    }
}
