# Build stage
FROM maven:3.9.9-amazoncorretto-21-debian AS build
WORKDIR /app

COPY src ./src
COPY pom.xml .

RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:21
WORKDIR /app

COPY --from=build /app/target/payment-service.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]