package com.wangyy.multilanes.demo.rabbitmq.appd;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = {"com.wangyy.multilanes"})
public class AppD {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AppD.class).run(args);
    }
}
