FROM postgres:12.2-alpine

ENV DOCKER_ENTRYPPOINT_INITD_DIR /docker-entrypoint-initdb.d

RUN mkdir -p -m 774 $DOCKER_ENTRYPPOINT_INITD_DIR

COPY ./src/main/resources/scripts/alertdb-healthcheck.sh /usr/local/bin/alertdb-healthcheck.sh
RUN chmod 774 /usr/local/bin/alertdb-healthcheck.sh
