FROM eclipse-temurin:11-jre
ARG JAR_FILE=services/notification-adapter/target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
