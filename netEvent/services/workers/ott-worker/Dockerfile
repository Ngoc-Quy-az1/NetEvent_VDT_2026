FROM eclipse-temurin:11-jre
WORKDIR /app
COPY services/workers/ott-worker/target/ott-worker-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
