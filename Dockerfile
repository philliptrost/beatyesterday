# Stage 1: Build frontend
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build backend
FROM gradle:8.12-jdk21 AS backend-build
WORKDIR /app
COPY backend/ ./
RUN gradle bootJar --no-daemon

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy backend JAR
COPY --from=backend-build /app/build/libs/*.jar app.jar

# Copy frontend build into Spring Boot's static resources
# Spring Boot serves files from /static/ automatically
COPY --from=frontend-build /app/frontend/dist/ /app/static/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.web.resources.static-locations=file:/app/static/"]
