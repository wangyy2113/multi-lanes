package com.wangyy.multilanes.demo.finagle.appb.service;

import com.twitter.util.Future;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.demo.finagle.api.A2Bservice;
import com.wangyy.multilanes.demo.finagle.api.B2Cservice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ServiceImpl implements A2Bservice.FutureIface {

    @Autowired
    private B2Cservice.FutureIface b2cIface;

    @Override
    public Future<String> a2b(String msg) {
        log.info("receive msg:{}", msg);
        String newMsg = msg + " => " + String.format("[%s-line::B_%s]", FeatureTagContext.getDEFAULT(), FeatureTagContext.get());
        String res = b2cIface.b2c(newMsg).get();
        return Future.value(res);
    }
}
