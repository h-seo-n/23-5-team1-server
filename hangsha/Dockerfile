FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Xmx400m", "-jar", "/app/app.jar"]