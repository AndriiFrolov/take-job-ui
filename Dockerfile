# syntax=docker/dockerfile:1
FROM eclipse-temurin:17.0.6_10-jdk-focal

EXPOSE 8080
VOLUME /tmp
COPY target/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","app/app.jar"]
