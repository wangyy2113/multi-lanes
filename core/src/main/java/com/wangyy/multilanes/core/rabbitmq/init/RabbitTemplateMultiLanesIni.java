package com.wangyy.multilanes.core.rabbitmq.init;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.rabbitmq.trace.RabbitFeatureTagAfterReceiveAppender;
import com.wangyy.multilanes.core.rabbitmq.trace.RabbitFeatureTagBeforePublishAppender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/*
 * RabbitTemplate增加消息发送前 & 接收后处理器，用于featureTag打标
 *
 */
@ConditionalOnConfig("multi-lanes.rabbit.enable")
@Slf4j
@Component
public class RabbitTemplateMultiLanesIni {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void registerBeforePublishProcessor() {
        log.info("[multi-lanes] RabbitMQ registerProcessor...");

        rabbitTemplate.addBeforePublishPostProcessors(RabbitFeatureTagBeforePublishAppender.instance());

        rabbitTemplate.addAfterReceivePostProcessors(RabbitFeatureTagAfterReceiveAppender.instance());
    }

}
