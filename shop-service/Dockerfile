# Build stage
FROM maven:3.9.9-amazoncorretto-21-debian AS build
WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests -pl shop-service -am

# Run stage
FROM openjdk:21
WORKDIR /app

COPY --from=build /app/shop-service/target/shop-service.jar shop-service.jar

ENTRYPOINT ["java", "-jar", "shop-service.jar"]