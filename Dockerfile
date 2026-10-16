
# 1. Build the React frontend
FROM node:22-alpine AS frontend-build
WORKDIR /frontend
COPY .cleanloop-frontend/package*.json ./
RUN npm install
COPY .cleanloop-frontend/ ./
RUN npm run build

# 2. Build the Spring Boot backend with Java 21
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY --from=frontend-build /frontend/dist/ ./src/main/resources/static/
RUN mvn clean package -DskipTests

# 3. Run the combined application
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
