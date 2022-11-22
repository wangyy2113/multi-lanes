package com.wangyy.multilanes.core.rabbitmq.service;

import org.springframework.amqp.core.Exchange;

public interface RabbitNodeWatcher {

    boolean isExchangeExist(String exchange);

    void registerExchange(Exchange exchange);
}
