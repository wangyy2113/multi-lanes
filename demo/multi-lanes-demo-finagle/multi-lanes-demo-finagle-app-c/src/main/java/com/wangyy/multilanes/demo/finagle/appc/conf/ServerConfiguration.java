package com.wangyy.multilanes.demo.finagle.appc.conf;

import com.twitter.finagle.ListeningServer;
import com.twitter.finagle.Thrift;
import com.twitter.util.Duration;
import com.typesafe.config.ConfigFactory;
import com.wangyy.multilanes.demo.finagle.api.B2Cservice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Slf4j
@Configuration
public class ServerConfiguration {

    @Autowired
    private B2Cservice.FutureIface futureIface;

    @Bean
    public ListeningServer rpcServer() {
        int port = ConfigFactory.load().getInt("finagle.port");
        String zkHost = ConfigFactory.load().getString("finagle.zkHost");
        String zkPath = "/service/b2c";

        String label = "finagle-demo-app-c-client";
        ListeningServer server = Thrift.server()
                .withLabel(label)
                .withRequestTimeout(Duration.fromSeconds(30))
                .serveIface(new InetSocketAddress(port), futureIface);

        server.announce(String.format("zk!%s!%s!0", zkHost, zkPath)).get();
        log.info("ListeningServer started!!!!!");

        return server;
    }
}
