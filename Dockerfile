FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline
COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN useradd --system --create-home --shell /usr/sbin/nologin naty
COPY --from=build /build/target/*.jar app.jar
USER naty
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
