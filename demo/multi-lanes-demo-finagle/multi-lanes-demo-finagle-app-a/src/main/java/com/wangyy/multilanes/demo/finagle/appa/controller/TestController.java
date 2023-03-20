package com.wangyy.multilanes.demo.finagle.appa.controller;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.demo.finagle.api.A2Bservice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/finagle")
public class TestController {

    @Autowired
    private A2Bservice.FutureIface futureIface;

    @GetMapping("/test")
    public Object echo() {
        String msg = String.format("[%s-line::A_%s]", FeatureTagContext.getDEFAULT(), FeatureTagContext.get());
        return futureIface.a2b(msg).get();
    }
}
