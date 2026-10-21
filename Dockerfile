# Stage 1: Build Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .
RUN mvn -B clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/target/jobportal.jar app.jar

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar app.jar"]