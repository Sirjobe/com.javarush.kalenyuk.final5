# Build stage
FROM maven:3.9.9-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -Pprod -DskipTests

# Runtime stage
FROM openjdk:17-slim
RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY wait-for-db.sh wait-for-db.sh
RUN chmod +x wait-for-db.sh
COPY --from=build /app/target/*.jar app.jar
RUN groupadd -r jirauser && useradd -r -g jirauser jirauser
RUN chown -R jirauser:jirauser /app
USER jirauser
EXPOSE 8080
ENTRYPOINT ["./wait-for-db.sh", "db", "java", "-jar", "app.jar"]