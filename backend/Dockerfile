# Multi-stage build for SLA Monitoring Backend

FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl \
    && addgroup -S sla \
    && adduser -S sla -G sla

USER sla

COPY --from=build /app/target/sla-monitoring-*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=prod,docker

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
