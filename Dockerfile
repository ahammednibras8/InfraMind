FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

COPY src src

RUN ./mvnw clean package -DskipTests

EXPOSE 8081

ENTRYPOINT [ "java", "-jar", "/app/target/inframind-0.0.1-SNAPSHOT.jar" ]