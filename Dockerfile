# Stage 1: Build
FROM maven:3.9.1-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/SpringAI-RAG-ChatBot-imageGen-speechGen-imageProcess-0.0.1-SNAPSHOT.jar app.jar
ENV PORT 9090
EXPOSE 909
ENTRYPOINT ["java", "-jar", "app.jar"]
