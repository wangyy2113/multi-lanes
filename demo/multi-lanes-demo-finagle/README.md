# multi-lanes Finagle/Rpc
---------------

支持Finagle的泳道实现(暂时只支持zk注册发现方式)



运行DEMO
---------------

##### 运行finagle DEMO



1. clone
```sh
git clone https://github.com/wangyy2113/multi-lanes.git
cd demo/multi-lanes-demo-finagle
```

2. start docker-compose
```sh
sh bootstrap.sh
```
部署6个服务，其中：
base泳道：     A B C D
feature-x泳道：  B   D

3. 发送一个Header中带featureTag=feature-x的HTTP测试请求至 multi-lanes-demo-finagle-app-a_base_line 服务, 返回信息即为路由路径
```sh
curl -H 'featureTag:feature-x' -G 127.0.0.1:8006/finagle/test
```
得到 [base-line::A_feature-x] => [feature-x-line::B_feature-x] => [base-line::C_feature-x] => [feature-x-line::D_feature-x]
表示
（1）base泳道的A服务收到请求后打上feature-x tag 并rpc调用B服务finagle接口
（2）feature-x泳道的B服务收到这个请求后打上feature-x tag 并调用C服务finagle接口
（3）base泳道的C服务(因为不存在feature-x泳道的C服务)收到请求后打上feature-x tag 并调用D服务finagle接口
（4）feature-x泳道的D服务收到请求后打上feature-x tag 并返回结果


4. 发送一个不带featureTag的测试请求至 multi-lanes-demo-finagle-app-a_base_line 服务
```sh
curl -G 127.0.0.1:8006/finagle/test
```
得到 [base-line::A_base] => [base-line::B_base] => [base-line::C_base] => [base-line::D_base]

5. stop all
```sh
sh stop-all.sh
```

