# Use Maven to build the app
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . /app
RUN mvn clean package -DskipTests

# Use JDK to run the app
FROM openjdk:17-jdk
WORKDIR /app
COPY --from=build /app/target/time-deposit-kata-1.0-SNAPSHOT.jar /app/time-deposit-kata-1.0-SNAPSHOT.jar
EXPOSE 8080
CMD ["java", "-jar", "time-deposit-kata-1.0-SNAPSHOT.jar"]