package com.wangyy.multilanes.core.init;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.trace.rabbit.RabbitFeatureTagAfterReceiveAppender;
import com.wangyy.multilanes.core.trace.rabbit.RabbitFeatureTagBeforePublishAppender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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
