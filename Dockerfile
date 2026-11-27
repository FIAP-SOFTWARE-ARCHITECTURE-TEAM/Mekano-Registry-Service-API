# --- Stage 1: Build ---
FROM registry.access.redhat.com/ubi9/openjdk-17:1.24 AS build
USER root
RUN microdnf install -y unzip && microdnf clean all
WORKDIR /workspace

# Apenas wrapper + POMs primeiro (cache de dependências)
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY registry-domain/pom.xml registry-domain/pom.xml
COPY registry-application/pom.xml registry-application/pom.xml
COPY registry-infrastructure/pom.xml registry-infrastructure/pom.xml
COPY registry-rest/pom.xml registry-rest/pom.xml

# Converte CRLF → LF (Windows), ajusta permissões e dá posse ao UID 185
RUN find . \( -name "mvnw" -o -name "*.sh" -o -name "maven-wrapper.properties" \) -exec sed -i 's/\r$//' {} + \
    && chmod +x mvnw \
    && chown -R 185:0 /workspace

USER 185
RUN ./mvnw -B -ntp dependency:go-offline -DskipTests || true

# Código-fonte
COPY --chown=185:0 registry-domain registry-domain
COPY --chown=185:0 registry-application registry-application
COPY --chown=185:0 registry-infrastructure registry-infrastructure
COPY --chown=185:0 registry-rest registry-rest

# Sem testes no build da imagem (rodar no CI)
RUN ./mvnw -B -ntp package -pl registry-rest -am -DskipTests

# --- Stage 2: Runtime ---
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:1.24

ENV LANGUAGE='en_US:en'

COPY --from=build --chown=185:0 /workspace/registry-rest/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185:0 /workspace/registry-rest/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185:0 /workspace/registry-rest/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185:0 /workspace/registry-rest/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8081
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=8081 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]