FROM eclipse-temurin:11-jre
WORKDIR /app
COPY services/workers/sms-worker/target/sms-worker-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
