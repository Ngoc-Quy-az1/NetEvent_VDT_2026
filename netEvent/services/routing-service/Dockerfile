FROM openjdk:11-jre-slim
WORKDIR /app
COPY services/routing-service/target/routing-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
