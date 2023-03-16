#!/bin/sh

set -e

sh /wait-for-it/wait-for.sh "zookeeper:2181" "--timeout=20" "--" "echo" "zookeeper started"

cmd="$@"
exec $cmd