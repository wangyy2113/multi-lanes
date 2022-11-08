package com.wangyy.multilanes.core.rabbitmq.trace;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

public class RabbitFeatureTagBeforePublishAppender implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().setHeader(FeatureTagContext.NAME, FeatureTagContext.get());
        return message;
    }

    public static RabbitFeatureTagBeforePublishAppender instance() {
        return InstanceHolder.APPENDER;
    }

    private static class InstanceHolder {
        private static final RabbitFeatureTagBeforePublishAppender APPENDER = new RabbitFeatureTagBeforePublishAppender();
    }

}
