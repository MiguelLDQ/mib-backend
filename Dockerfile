# ---------- Estagio de build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Estagio de execucao ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S mib && adduser -S mib -G mib
COPY --from=build /app/target/mib-backend.jar app.jar
USER mib
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
