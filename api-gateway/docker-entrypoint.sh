#!/bin/sh
exec java $JAVA_OPTS \
    -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
    -Dserver.port=$SERVER_PORT \
    -Dmanagement.server.port=$SERVER_PORT \
    -jar api-gateway.jar 