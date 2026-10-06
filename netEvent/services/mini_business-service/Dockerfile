FROM eclipse-temurin:11-jre
ARG JAR_FILE=services/mini_business-service/target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app.jar"]
