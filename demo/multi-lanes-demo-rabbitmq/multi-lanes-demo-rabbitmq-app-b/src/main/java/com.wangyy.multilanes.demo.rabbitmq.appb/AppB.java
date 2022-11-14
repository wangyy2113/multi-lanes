package com.wangyy.multilanes.demo.rabbitmq.appb;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = {"com.wangyy.multilanes"})
public class AppB {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AppB.class).run(args);
    }
}
