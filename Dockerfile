
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

RUN ./mvnw package -DskipTests

ENTRYPOINT ["java", "-jar", "target/project-tracker-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080