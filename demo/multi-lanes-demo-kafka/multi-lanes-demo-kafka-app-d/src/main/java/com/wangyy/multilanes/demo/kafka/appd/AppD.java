package com.wangyy.multilanes.demo.kafka.appd;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Created by houyantao on 2023/1/11
 */
@SpringBootApplication(scanBasePackages = {"com.wangyy.multilanes"})
public class AppD {
    public static void main(String[] args) {
        new SpringApplicationBuilder(AppD.class).run(args);

    }
}
