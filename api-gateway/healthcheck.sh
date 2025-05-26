#!/bin/sh
curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1 