# Stage 1: Build frontend
FROM node:22-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install
COPY frontend/ .
RUN npm run build

# Stage 2: Build backend with frontend embedded
FROM eclipse-temurin:23-jdk AS backend
WORKDIR /app
COPY backend/pactum_challange/mvnw backend/pactum_challange/pom.xml ./
COPY backend/pactum_challange/.mvn .mvn
RUN chmod +x mvnw
COPY backend/pactum_challange/src src
COPY --from=frontend /app/dist src/main/resources/static/
RUN ./mvnw package -DskipTests -B

# Stage 3: Runtime
FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=backend /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
