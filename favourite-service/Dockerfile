FROM eclipse-temurin:11-jre-alpine

ARG PROJECT_VERSION=0.1.0
ARG JAR_FILE=target/favourite-service-v${PROJECT_VERSION}.jar

# Create app directory
WORKDIR /app

# Add a non-root user to run the application
RUN addgroup -S spring && adduser -S spring -G spring

# Environment variables
ENV SPRING_PROFILES_ACTIVE=dev
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery:8761/eureka/
ENV SPRING_CLOUD_CONFIG_URI=http://cloud-config:9296
ENV USER_SERVICE_HOST=http://user-service:8700
ENV PRODUCT_SERVICE_HOST=http://product-service:8500

# Copy the JAR file
COPY ${JAR_FILE} app.jar

# Change ownership to spring user
RUN chown -R spring:spring /app
USER spring

# Expose port
EXPOSE 8800

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD wget --no-verbose --tries=1 --spider http://localhost:8800/favourite-service/actuator/health || exit 1

# Run the jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]