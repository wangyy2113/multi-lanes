#!/bin/bash

APP_JAR=$1
java -Dconfig.override_with_env_vars=true -jar ${APP_JAR}
