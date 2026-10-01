FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN chmod +x ./mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

RUN useradd --system --uid 10001 --create-home appuser
WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/target/ordercraft-backend-1.0.0.jar app.jar
USER 10001

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
