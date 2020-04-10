FROM postgres:11.6-alpine

ENV POSTGRES_DB alertdb
ENV POSTGRES_USER sa
ENV POSTGRES_PASSWORD blackduck

ENV DOCKER_ENTRYPPOINT_INITD_DIR /docker-entrypoint-initdb.d

RUN mkdir -p -m 774 $DOCKER_ENTRYPPOINT_INITD_DIR

COPY ./src/main/resources/scripts/init_alert_db.sql $DOCKER_ENTRYPPOINT_INITD_DIR/init_alert_db.sql
COPY ./src/main/resources/scripts/alertdb-healthcheck.sh /usr/local/bin/alertdb-healthcheck.sh
RUN chmod 774 /usr/local/bin/alertdb-healthcheck.sh
