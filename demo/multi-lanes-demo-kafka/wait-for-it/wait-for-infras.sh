#!/bin/sh

set -e

sh /wait-for-it/wait-for.sh "zookeeper:2181" "--timeout=20" "--" "echo" "zookeeper started"
sh /wait-for-it/wait-for.sh "kafka:9092" "--timeout=60" "--" "echo" "kafka started"

cmd="$@"
exec $cmd