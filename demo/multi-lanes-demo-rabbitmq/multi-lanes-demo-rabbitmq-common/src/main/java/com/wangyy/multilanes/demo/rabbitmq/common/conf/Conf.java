package com.wangyy.multilanes.demo.rabbitmq.common.conf;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public abstract class Conf {

    static final Config config = ConfigFactory.load();



}
