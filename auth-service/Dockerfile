FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and grant execution permission
COPY mvnw .
COPY .mvn ./.mvn
COPY pom.xml .
RUN chmod +x ./mvnw

# Cache dependencies (avoids downloading all JARs on every build)
RUN ./mvnw dependency:go-offline -B

# Copy source code and build JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]