package com.wangyy.multilanes.demo.rabbitmq.appb.controller;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.demo.rabbitmq.common.data.TestMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/B/test")
public class TestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/rabbit")
    public Object send(@RequestParam(value = "exchange") String exchange) {
        String msg = String.format("[%s-line::B_%s]", FeatureTagContext.getDEFAULT(), FeatureTagContext.get());
        rabbitTemplate.convertAndSend(exchange, "", new TestMsg(msg));
        return "suc " + System.currentTimeMillis();
    }
}
