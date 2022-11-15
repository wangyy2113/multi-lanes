#!/bin/sh

echo "
#############################################

build core

#############################################
"
cd ../../core
mvn clean install

echo "
#############################################

build multi-lanes-demo-rabbitmq

#############################################
"
cd ../demo/multi-lanes-demo-rabbitmq/
mvn clean install

echo "
#############################################

build images

#############################################
"

cd multi-lanes-demo-rabbitmq-app-a
docker build --no-cache -t multi-lanes-demo-rabbitmq-app-a .

cd .. && cd multi-lanes-demo-rabbitmq-app-b
docker build --no-cache -t multi-lanes-demo-rabbitmq-app-b .

cd .. && cd multi-lanes-demo-rabbitmq-app-c
docker build --no-cache -t multi-lanes-demo-rabbitmq-app-c .

cd .. && cd multi-lanes-demo-rabbitmq-app-d
docker build --no-cache -t multi-lanes-demo-rabbitmq-app-d .


echo "
#############################################

docker-compose start begin

#############################################
"

cd ..


docker-compose -f docker-compose.yml up -d



sh wait-for-it/wait-for.sh "127.0.0.1:2181" "--timeout=30" "--" "echo" "zookeeper started"
sh wait-for-it/wait-for.sh "127.0.0.1:5672" "--timeout=60" "--" "echo" "rabbitmq started"

sh wait-for-it/wait-for.sh "127.0.0.1:8006" "--timeout=80" "--" "echo" "multi-lanes-demo-rabbitmq-app-a_base_line started"
sh wait-for-it/wait-for.sh "127.0.0.1:8007" "--timeout=80" "--" "echo" "multi-lanes-demo-rabbitmq-app-b_base_line started"
sh wait-for-it/wait-for.sh "127.0.0.1:8008" "--timeout=80" "--" "echo" "multi-lanes-demo-rabbitmq-app-c_base_line started"
sh wait-for-it/wait-for.sh "127.0.0.1:8009" "--timeout=80" "--" "echo" "multi-lanes-demo-rabbitmq-app-d_base_line started"

sh wait-for-it/wait-for.sh "127.0.0.1:8017" "--timeout=80" "--" "echo" "multi-lanes-demo-rabbitmq-app-b_feature-x_line started"
sh wait-for-it/wait-for.sh "127.0.0.1:8019" "--timeout=80" "--" "echo" "multi-lanes-demo-rabbitmq-app-d_feature-x_line started"

echo "
#############################################

docker-compose started

#############################################
"