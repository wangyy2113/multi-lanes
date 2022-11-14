package com.wangyy.multilanes.demo.rabbitmq.appc.listener;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.demo.rabbitmq.common.data.TestMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@RabbitListener(queues = "c_queue")
@Slf4j
@Component
public class TestListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 接收AppB的消息，并发送给AppD
     *
     */
    @RabbitHandler
    public void onMessage(@Payload TestMsg message) {
        String msg = message.getMsg() + " => " + String.format("[C_%s]", FeatureTagContext.getDEFAULT());
        log.info("[multi-lanes=RabbitMQ AppC] featureTag:{} route: {}", FeatureTagContext.get(), msg);


        new Thread(() -> rabbitTemplate.convertAndSend("d_exchange", "", new TestMsg(msg))).start();
    }
}
