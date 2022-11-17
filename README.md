# multi-lanes
---------------

多泳道环境建设

理论参考 https://developer.aliyun.com/article/700766

环境准备
docker & docker-compose & jdk1.8 & maven



支持项
---------------

> * HTTP
> * RPC       TODO
> * kafka     TODO
> * RabbitMQ



Getting Started
---------------

1. 引入maven dependency

```xml
<dependency>
  <groupId>com.wangyy</groupId>
  <artifactId>multi-lanes-core</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

2. 配置文件开启multi-lines

multi-lanes.enable = true

增加泳道标志配置项(optional)

featureTag = xxx

运行DEMO
---------------

##### 运行RabbitMQ DEMO

![image](https://github.com/wangyy2113/multi-lanes/blob/main/demo/multi-lanes-demo-rabbitmq/multi-lanes-demo-rabbitmq.png)

1. clone
```sh
git clone https://github.com/wangyy2113/multi-lanes.git
cd demo/multi-lanes-demo-rabbitmq
```

2. start docker-compose
```sh
sh bootstrap.sh
```

3. 发送一个Header中带featureTag=feature-x的HTTP测试请求至 multi-lanes-demo-rabbitmq-app-a_base_line 服务
```sh
curl -H 'featureTag:feature-x' -G -d 'exchange=b_exchange' 127.0.0.1:8006/A/test/rabbit
```


4. 观察泳道中各服务rabbitMQ处理相关日志，例如查看multi-lanes-demo-rabbitmq-app-d_feature-x_line 判断是否符合预期
```sh
docker logs $(docker ps|grep multi-lanes-demo-rabbitmq-app-d_feature-x_line| awk '{print $1}') |grep 'multi-lanes=RabbitMQ'
```

5. 发送一个不带featureTag的测试请求至 multi-lanes-demo-rabbitmq-app-a_base_line 服务
```sh
curl -G -d 'exchange=b_exchange' 127.0.0.1:8006/A/test/rabbit
```

6. 观察泳道中各服务rabbitMQ处理相关日志，例如查看multi-lanes-demo-rabbitmq-app-d_base_line 判断是否符合预期
```sh
docker logs $(docker ps|grep multi-lanes-demo-rabbitmq-app-d_base_line| awk '{print $1}') |grep 'multi-lanes=RabbitMQ'
```

7. stop all
```sh
sh stop-all.sh
```

