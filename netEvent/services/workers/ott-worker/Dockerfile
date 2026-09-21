FROM openjdk:11-jre-slim
WORKDIR /app
COPY services/workers/ott-worker/target/ott-worker-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
