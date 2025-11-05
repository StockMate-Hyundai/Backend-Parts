# Dockerfile

# jdk17 Image Start
FROM eclipse-temurin:17-jdk-alpine

ARG JAR_FILE=build/libs/parts-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} parts_Backend.jar
ENTRYPOINT ["java","-jar","-Duser.timezone=Asia/Seoul","parts_Backend.jar"]
