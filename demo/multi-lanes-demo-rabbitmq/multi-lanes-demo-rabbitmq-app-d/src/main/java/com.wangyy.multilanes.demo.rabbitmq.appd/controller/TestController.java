package com.wangyy.multilanes.demo.rabbitmq.appd.controller;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.demo.rabbitmq.common.data.TestMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/D/test")
public class TestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/rabbit")
    public Object reload(@RequestParam(value = "exchange") String exchange) {
        String msg = String.format("[%s-line::D_%s]", FeatureTagContext.getDEFAULT(), FeatureTagContext.get());
        rabbitTemplate.convertAndSend(exchange, "", new TestMsg(msg));
        return "suc " + System.currentTimeMillis();
    }
}
