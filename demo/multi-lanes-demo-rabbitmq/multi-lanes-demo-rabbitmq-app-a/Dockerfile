FROM openjdk:8-jdk-alpine

ADD target/multi-lanes-demo-rabbitmq-app-a-1.0-SNAPSHOT.jar multi-lanes-demo-rabbitmq-app-a.jar
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -jar /multi-lanes-demo-rabbitmq-app-a.jar
