package com.wangyy.multilanes.demo.finagle.appc.service;

import com.twitter.util.Future;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.demo.finagle.api.B2Cservice;
import com.wangyy.multilanes.demo.finagle.api.C2Dservice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ServiceImpl implements B2Cservice.FutureIface {

    @Autowired
    private C2Dservice.FutureIface c2dIface;

    @Override
    public Future<String> b2c(String msg) {
        log.info("receive msg:{}", msg);
        String newMsg = msg + " => " + String.format("[%s-line::C_%s]", FeatureTagContext.getDEFAULT(), FeatureTagContext.get());
        String res = c2dIface.c2d(newMsg).get();
        return Future.value(res);
    }
}
