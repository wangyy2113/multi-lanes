package com.wangyy.multilanes.demo.rabbitmq.appc;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = {"com.wangyy.multilanes"})
public class AppC {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AppC.class).run(args);
    }
}
