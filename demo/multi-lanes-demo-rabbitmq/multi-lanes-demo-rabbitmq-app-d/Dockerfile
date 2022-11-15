FROM openjdk:8-jdk-alpine

ADD target/multi-lanes-demo-rabbitmq-app-d-1.0-SNAPSHOT.jar multi-lanes-demo-rabbitmq-app-d.jar
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -jar /multi-lanes-demo-rabbitmq-app-d.jar
