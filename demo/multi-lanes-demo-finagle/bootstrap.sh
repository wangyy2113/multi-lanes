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

build multi-lanes-demo-finagle

#############################################
"
cd ../demo/multi-lanes-demo-finagle/
mvn clean install

echo "
#############################################

build images

#############################################
"

cd multi-lanes-demo-finagle-app-a
docker build --no-cache -t multi-lanes-demo-finagle-app-a .

cd .. && cd multi-lanes-demo-finagle-app-b
docker build --no-cache -t multi-lanes-demo-finagle-app-b .

cd .. && cd multi-lanes-demo-finagle-app-c
docker build --no-cache -t multi-lanes-demo-finagle-app-c .

cd .. && cd multi-lanes-demo-finagle-app-d
docker build --no-cache -t multi-lanes-demo-finagle-app-d .


echo "
#############################################

docker-compose start begin

#############################################
"

cd ..


docker-compose -f docker-compose.yml up -d



sh wait-for-it/wait-for.sh "127.0.0.1:2181" "--timeout=30" "--" "echo" "zookeeper started"

sh wait-for-it/wait-for.sh "127.0.0.1:8006" "--timeout=80" "--" "echo" "multi-lanes-demo-finagle-app-a_base_line started"
sh wait-for-it/wait-for.sh "127.0.0.1:8007" "--timeout=80" "--" "echo" "multi-lanes-demo-finagle-app-b_base_line started"
sh wait-for-it/wait-for.sh "127.0.0.1:8008" "--timeout=80" "--" "echo" "multi-lanes-demo-finagle-app-c_base_line started"
sh wait-for-it/wait-for.sh "127.0.0.1:8009" "--timeout=80" "--" "echo" "multi-lanes-demo-finagle-app-d_base_line started"

sh wait-for-it/wait-for.sh "127.0.0.1:8017" "--timeout=80" "--" "echo" "multi-lanes-demo-finagle-app-b_feature-x_line started"
sh wait-for-it/wait-for.sh "127.0.0.1:8019" "--timeout=80" "--" "echo" "multi-lanes-demo-finagle-app-d_feature-x_line started"

echo "
#############################################

docker-compose started

#############################################
"