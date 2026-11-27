# Build multi-stage do Mekano Registry Service API
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY registry-domain/pom.xml registry-domain/pom.xml
COPY registry-application/pom.xml registry-application/pom.xml
COPY registry-infrastructure/pom.xml registry-infrastructure/pom.xml
COPY registry-rest/pom.xml registry-rest/pom.xml

RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline -DskipTests || true

COPY registry-domain registry-domain
COPY registry-application registry-application
COPY registry-infrastructure registry-infrastructure
COPY registry-rest registry-rest

RUN ./mvnw -B -DskipTests package -pl registry-rest -am

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd -r quarkus && useradd -r -g quarkus quarkus
COPY --from=build /workspace/registry-rest/target/quarkus-app/ /app/
RUN chown -R quarkus:quarkus /app
USER quarkus

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/quarkus-run.jar"]
