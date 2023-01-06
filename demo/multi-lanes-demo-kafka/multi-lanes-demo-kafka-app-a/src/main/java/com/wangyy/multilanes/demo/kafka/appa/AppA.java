package com.wangyy.multilanes.demo.kafka.appa;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Created by houyantao on 2023/1/4
 */
@SpringBootApplication(scanBasePackages = {"com.wangyy.multilanes"})
public class AppA {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AppA.class).run(args);
    }
}
