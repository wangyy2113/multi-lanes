package com.wangyy.multilanes.demo.rabbitmq.appd.listener;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.demo.rabbitmq.common.data.TestMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@RabbitListener(queues = "d_queue")
@Slf4j
@Component
public class TestListener {

    /**
     * 接收AppC的消息
     *
     */
    @RabbitHandler
    public void onMessage(@Payload TestMsg message) {
        String msg = message.getMsg() + " => " + String.format("[D_%s]", FeatureTagContext.getDEFAULT());
        log.info("[multi-lanes=RabbitMQ AppD] featureTag:{} route: {}", FeatureTagContext.get(), msg);
    }
}
