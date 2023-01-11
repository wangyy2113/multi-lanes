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

build multi-lanes-demo-kafka

#############################################
"
cd ../demo/multi-lanes-demo-kafka/
mvn clean install

echo "
#############################################

build images

#############################################
"

cd multi-lanes-demo-kafka-app-a
docker build --no-cache -t multi-lanes-demo-kafka-app-a .

cd .. && cd multi-lanes-demo-kafka-app-b
docker build --no-cache -t multi-lanes-demo-kafka-app-b .

cd .. && cd multi-lanes-demo-kafka-app-c
docker build --no-cache -t multi-lanes-demo-kafka-app-c .

cd .. && cd multi-lanes-demo-kafka-app-d
docker build --no-cache -t multi-lanes-demo-kafka-app-d .


echo "
#############################################

docker-compose start begin

#############################################
"

cd ..


docker-compose -f docker-compose.yml up -d



sh wait-for-it/wait-for.sh "127.0.0.1:2181" "--timeout=30" "--" "echo" "zookeeper started"
sh wait-for-it/wait-for.sh "127.0.0.1:9092" "--timeout=60" "--" "echo" "kafka started"

sh wait-for-it/wait-for.sh "127.0.0.1:9001" "--timeout=80" "--" "echo" "multi-lanes-demo-kafka-app-a_base_line started"
sh wait-for-it/wait-for.sh "127.0.0.1:9002" "--timeout=80" "--" "echo" "multi-lanes-demo-kafka-app-b_base_line started"
sh wait-for-it/wait-for.sh "127.0.0.1:9003" "--timeout=80" "--" "echo" "multi-lanes-demo-kafka-app-c_base_line started"
sh wait-for-it/wait-for.sh "127.0.0.1:9004" "--timeout=80" "--" "echo" "multi-lanes-demo-kafka-app-d_base_line started"

sh wait-for-it/wait-for.sh "127.0.0.1:9102" "--timeout=80" "--" "echo" "multi-lanes-demo-kafka-app-b_feature-x_line started"
sh wait-for-it/wait-for.sh "127.0.0.1:9104" "--timeout=80" "--" "echo" "multi-lanes-demo-kafka-app-d_feature-x_line started"

echo "
#############################################

docker-compose started

#############################################
"