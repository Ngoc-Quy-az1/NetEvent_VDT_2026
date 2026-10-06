FROM eclipse-temurin:11-jre
ARG JAR_FILE=services/kpi-simulator-service/target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "/app.jar"]
