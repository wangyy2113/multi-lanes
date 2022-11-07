package com.wangyy.multilanes.core.trace.rabbit;

import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

public class RabbitFeatureTagAfterReceiveAppender implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        Object featureTagObj = message.getMessageProperties().getHeaders().get(FTConstants.FEATURE_TAG_PATH);
        String featureTag;
        if (featureTagObj == null) {
            featureTag = FeatureTagContext.getDEFAULT();
        } else {
            featureTag = featureTagObj.toString();
            //TODO check featureTag illegal
        }
        //设置本次请求featureTag
        FeatureTagContext.set(featureTag);
        return message;
    }

    public static RabbitFeatureTagAfterReceiveAppender instance() {
        return InstanceHolder.APPENDER;
    }

    private static class InstanceHolder {
        private static final RabbitFeatureTagAfterReceiveAppender APPENDER = new RabbitFeatureTagAfterReceiveAppender();
    }

}
