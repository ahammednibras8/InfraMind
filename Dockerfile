FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

COPY src src
COPY javaagent javaagent/

RUN ./mvnw clean package -DskipTests

EXPOSE 8081