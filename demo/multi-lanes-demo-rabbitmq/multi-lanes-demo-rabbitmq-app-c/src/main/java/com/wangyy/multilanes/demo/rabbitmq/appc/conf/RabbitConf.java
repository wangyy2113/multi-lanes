package com.wangyy.multilanes.demo.rabbitmq.appc.conf;

import com.wangyy.multilanes.demo.rabbitmq.common.conf.RabbitConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitConf extends RabbitConstants {


    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, true);
    }

    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true, false, true);
    }

    @Bean
    public Declarables declarables(TopicExchange exchange, Queue queue) {
        List<Declarable> bindings = new ArrayList<>();
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
        bindings.add(binding);
        return new Declarables(bindings);
    }


    /*@Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory, TestListener testListener) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(1);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setQueueNames(ConfigFactory.load().getString("rabbit.queue"));
        container.setMessageListener(testListener);

        return container;
    }*/


}
