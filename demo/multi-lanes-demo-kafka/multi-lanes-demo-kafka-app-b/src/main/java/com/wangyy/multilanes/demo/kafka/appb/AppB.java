package com.wangyy.multilanes.demo.kafka.appb;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Created by houyantao on 2023/1/4
 */
@SpringBootApplication(scanBasePackages = {"com.wangyy.multilanes"})
public class AppB {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AppB.class).run(args);
    }
}
