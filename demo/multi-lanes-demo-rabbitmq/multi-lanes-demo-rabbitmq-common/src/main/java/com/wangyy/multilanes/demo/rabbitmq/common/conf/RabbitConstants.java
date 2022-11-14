package com.wangyy.multilanes.demo.rabbitmq.common.conf;

public abstract class RabbitConstants extends Conf {

   public static final String EXCHANGE = config.getString("rabbit.exchange");

   public static final String QUEUE = config.getString("rabbit.queue");

   public static final String ROUTING_KEY = config.getString("rabbit.routingKey");
}
