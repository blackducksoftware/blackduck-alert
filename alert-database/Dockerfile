FROM postgres:12.2-alpine

ENV DOCKER_ENTRYPPOINT_INITD_DIR /docker-entrypoint-initdb.d

RUN mkdir -p -m 774 $DOCKER_ENTRYPPOINT_INITD_DIR

COPY ./src/main/resources/scripts/alertdb-healthcheck.sh /usr/local/bin/alertdb-healthcheck.sh
COPY ./src/main/resources/scripts/create_extension.sql $DOCKER_ENTRYPPOINT_INITD_DIR/create_extension.sql

RUN chmod 774 /usr/local/bin/alertdb-healthcheck.sh
