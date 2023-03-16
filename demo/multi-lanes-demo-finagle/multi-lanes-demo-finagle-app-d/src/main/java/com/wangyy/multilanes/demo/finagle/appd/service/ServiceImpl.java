package com.wangyy.multilanes.demo.finagle.appd.service;

import com.twitter.util.Future;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.demo.finagle.api.C2Dservice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ServiceImpl implements C2Dservice.FutureIface {

    @Override
    public Future<String> c2d(String msg) {
        log.info("receive msg:{}", msg);
        String res = msg + " => " + String.format("[%s-line::D_%s]", FeatureTagContext.getDEFAULT(), FeatureTagContext.get());
        return Future.value(res);
    }
}
