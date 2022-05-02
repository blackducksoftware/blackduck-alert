FROM postgres:12.2-alpine

ARG VERSION

LABEL com.blackducksoftware.integration.alert.vendor="Black Duck Software, Inc." \
      com.blackducksoftware.integration.alert.version="$VERSION"

ENV DOCKER_ENTRYPPOINT_INITD_DIR /docker-entrypoint-initdb.d

RUN mkdir -p -m 774 $DOCKER_ENTRYPPOINT_INITD_DIR

COPY alertdb-healthcheck.sh /usr/local/bin/alertdb-healthcheck.sh
COPY create_extension.sql $DOCKER_ENTRYPPOINT_INITD_DIR/create_extension.sql

RUN chmod 774 /usr/local/bin/alertdb-healthcheck.sh