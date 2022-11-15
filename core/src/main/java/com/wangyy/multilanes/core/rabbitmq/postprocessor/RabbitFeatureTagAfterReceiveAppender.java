package com.wangyy.multilanes.core.rabbitmq.postprocessor;

import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

/*
 * 收到消息后根据header中的featureTag信息将当前线程流量打标
 *
 */
public class RabbitFeatureTagAfterReceiveAppender implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        Object featureTagObj = message.getMessageProperties().getHeaders().get(FTConstants.FEATURE_TAG_PATH);
        String featureTag;
        if (featureTagObj == null || featureTagObj.toString().equals(FTConstants.FEATURE_TAG_BASE_LANE_VALUE)) {
            //base-line的消息，打上当前line的tag
            featureTag = FeatureTagContext.getDEFAULT();
        } else {
            //非base-line的消息，维持tag
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
