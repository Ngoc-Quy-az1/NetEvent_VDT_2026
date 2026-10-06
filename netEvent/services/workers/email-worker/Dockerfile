FROM eclipse-temurin:11-jre
WORKDIR /app
COPY services/workers/email-worker/target/email-worker-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
