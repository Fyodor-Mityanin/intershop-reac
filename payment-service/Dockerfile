# Build stage
FROM maven:3.9.9-amazoncorretto-21-debian AS build
WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests -pl payment-service -am

# Run stage
FROM openjdk:21
WORKDIR /app

COPY --from=build /app/payment-service/target/payment-service.jar payment-service.jar

ENTRYPOINT ["java", "-jar", "payment-service.jar"]