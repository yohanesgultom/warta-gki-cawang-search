FROM maven:3.8.4-openjdk-17-slim AS builder
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN mvn clean test package

FROM openjdk:17-alpine
WORKDIR /opt/app
COPY --from=builder /usr/src/app/target/*.jar /opt/app/app.jar
