package com.wangyy.multilanes.demo.finagle.appa.conf;

import com.twitter.finagle.Thrift;
import com.twitter.util.Duration;
import com.typesafe.config.ConfigFactory;
import com.wangyy.multilanes.demo.finagle.api.A2Bservice;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class ClientConfiguration {

    @Bean
    public A2Bservice.FutureIface rpcService() {
        String label = "finagle-demo-app-a-client";
        String zkHost = ConfigFactory.load().getString("finagle.zkHost");
        String dest = String.format("zk!%s!/service/a2b", zkHost);

        return Thrift.client()
                .withLabel(label)
                .withProtocolFactory(new TBinaryProtocol.Factory())
                .withSessionPool().minSize(10)
                .withSessionPool().maxSize(50)
                .withSessionPool().maxWaiters(100)
                .withSessionQualifier().noFailFast()
                .withRequestTimeout(Duration.fromSeconds(3))
                .newIface(dest, label, A2Bservice.FutureIface.class);
    }
}
