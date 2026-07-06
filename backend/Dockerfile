# syntax=docker/dockerfile:1
# Multi-stage build for SLA Monitoring Backend

FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

COPY src ./src
# package (sans clean) + retries pour réseaux instables dans Docker
RUN --mount=type=cache,target=/root/.m2 \
    sh -c 'for i in 1 2 3 4 5; do mvn package -DskipTests -B && exit 0; echo "Maven retry $i/5..."; sleep 15; done; exit 1'

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
