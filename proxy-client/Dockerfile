FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app

COPY pom.xml ./

RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

ARG PROJECT_VERSION=0.1.0
ARG ENVIRONMENT=dev
ARG USER_ID=1001
ARG GROUP_ID=1001

ENV SPRING_PROFILES_ACTIVE=${ENVIRONMENT}
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"
ENV SERVER_PORT=8900
ENV EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery:8761/eureka/
ENV SPRING_CLOUD_CONFIG_URI=http://cloud-config:9296
ENV USER_SERVICE_HOST=http://user-service:8700
ENV PRODUCT_SERVICE_HOST=http://product-service:8500
ENV ORDER_SERVICE_HOST=http://order-service:8300
ENV FAVOURITE_SERVICE_HOST=http://favourite-service:8800
ENV SHIPPING_SERVICE_HOST=http://shipping-service:8600
ENV PAYMENT_SERVICE_HOST=http://payment-service:8400

RUN groupadd -g ${GROUP_ID} appuser && \
    useradd -r -u ${USER_ID} -g appuser appuser && \
    mkdir -p /home/app && chown -R appuser:appuser /home/app

WORKDIR /home/app
USER appuser

COPY --from=build --chown=appuser:appuser /app/target/proxy-client-v${PROJECT_VERSION}.jar proxy-client.jar

EXPOSE ${SERVER_PORT}

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dserver.port=$SERVER_PORT -Dmanagement.server.port=$SERVER_PORT -jar proxy-client.jar"]