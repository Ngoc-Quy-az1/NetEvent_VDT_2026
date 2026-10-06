FROM eclipse-temurin:11-jre
ARG JAR_FILE=services/notification-processor-service/target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
