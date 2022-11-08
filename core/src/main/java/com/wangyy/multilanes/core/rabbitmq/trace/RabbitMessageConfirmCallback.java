package com.wangyy.multilanes.core.rabbitmq.trace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitMessageConfirmCallback implements RabbitTemplate.ConfirmCallback {

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (correlationData == null) {
            log.warn("Receive publish confirm without correlationData. ack={}, cause={}", ack, cause);
            return;
        }

    }
}
