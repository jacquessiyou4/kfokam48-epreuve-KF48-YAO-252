# syntax=docker/dockerfile:1
# Construction : le cache ~/.m2 est conservé entre deux constructions (BuildKit).
# Délai de 60 s et nouvelles tentatives : une connexion bloquée ne fige plus la construction.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q package -DskipTests \
      -Daether.connector.requestTimeout=60000 \
      -Daether.connector.connectTimeout=20000 \
      -Daether.connector.http.retryHandler.count=5

# Exécution
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/presence-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
