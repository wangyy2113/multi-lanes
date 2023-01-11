FROM openjdk:8-jdk-alpine

ADD target/multi-lanes-demo-kafka-app-b-1.0-SNAPSHOT.jar multi-lanes-demo-kafka-app-b.jar
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -jar /multi-lanes-demo-kafka-app-b.jar
