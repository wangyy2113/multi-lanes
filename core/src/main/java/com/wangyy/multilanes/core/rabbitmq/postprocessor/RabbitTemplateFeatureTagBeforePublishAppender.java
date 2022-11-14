package com.wangyy.multilanes.core.rabbitmq.postprocessor;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

/*
 * RabbitTemplate消息发送前Header中设置featureTag信息
 *
 */
public class RabbitTemplateFeatureTagBeforePublishAppender implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().setHeader(FeatureTagContext.NAME, FeatureTagContext.get());
        return message;
    }

    public static RabbitTemplateFeatureTagBeforePublishAppender instance() {
        return InstanceHolder.APPENDER;
    }

    private static class InstanceHolder {
        private static final RabbitTemplateFeatureTagBeforePublishAppender APPENDER = new RabbitTemplateFeatureTagBeforePublishAppender();
    }

}
