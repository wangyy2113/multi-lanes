#!/bin/sh

set -e

sh /wait-for-it/wait-for.sh "zookeeper:2181" "--timeout=20" "--" "echo" "zookeeper started"
sh /wait-for-it/wait-for.sh "rabbitmq:5672" "--timeout=60" "--" "echo" "rabbitmq started"

cmd="$@"
exec $cmd