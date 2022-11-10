package com.wangyy.multilanes.core.rabbitmq;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.rabbitmq.init.RabbitDeclarableMultiLanesIni;
import com.wangyy.multilanes.core.rabbitmq.init.RabbitTemplateMultiLanesIni;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

/*
 * RabbitMQ 泳道资源初始化引导类
 *
 */
@ConditionalOnConfig("multi-lanes.rabbit.enable")
@Slf4j
@Component
public class RabbitMultiLanesBootstrap implements PriorityOrdered {

    public RabbitMultiLanesBootstrap(ApplicationContext applicationContext) throws Exception {
        new RabbitDeclarableMultiLanesIni(applicationContext).init();

        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        new RabbitTemplateMultiLanesIni(rabbitTemplate).init();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
