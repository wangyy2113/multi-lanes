package com.wangyy.multilanes.demo.finagle.appa;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = {"com.wangyy.multilanes"})
public class AppA {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AppA.class).run(args);
    }
}
