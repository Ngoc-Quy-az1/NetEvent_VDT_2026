FROM eclipse-temurin:11-jre
WORKDIR /app
COPY services/audit-service/target/audit-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
