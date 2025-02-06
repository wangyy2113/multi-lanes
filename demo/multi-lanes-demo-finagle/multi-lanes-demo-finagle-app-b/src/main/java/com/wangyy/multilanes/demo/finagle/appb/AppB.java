package com.wangyy.multilanes.demo.finagle.appb;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = {"com.wangyy.multilanes"})
public class AppB {

    //java -jar -DfeatureTag=testA multi-lanes-demo-finagle-app-b-1.0-SNAPSHOT.jar
    //java -jar -DfeatureTag=base multi-lanes-demo-finagle-app-b-1.0-SNAPSHOT.jar
    public static void main(String[] args) {
        new SpringApplicationBuilder(AppB.class).run(args);
    }
}
