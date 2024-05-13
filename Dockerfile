FROM gradle:8.7.0-jdk8 AS build
COPY  . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradle installDist

FROM eclipse-temurin:8-jre-ubi9-minimal
WORKDIR /app