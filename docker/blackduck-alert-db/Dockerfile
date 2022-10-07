# The ARG for the FROM image comes from Gradle. It is based off of postgresContainerVersion,
# and used in buildSrc/docker.gradle
ARG POSTGRESBASEIMAGE=""
FROM ${POSTGRESBASEIMAGE}

ARG VERSION

LABEL com.blackducksoftware.integration.alert.vendor="Black Duck Software, Inc." \
      com.blackducksoftware.integration.alert.version="$VERSION"

ENV DOCKER_ENTRYPPOINT_INITD_DIR /docker-entrypoint-initdb.d

USER root
RUN mkdir -p -m 774 $DOCKER_ENTRYPPOINT_INITD_DIR

COPY alertdb-healthcheck.sh /usr/local/bin/alertdb-healthcheck.sh
COPY blackduck-alert-db-cmd.sh /usr/local/bin/blackduck-alert-db-cmd.sh
COPY create_extension.sql $DOCKER_ENTRYPPOINT_INITD_DIR/create_extension.sql

RUN chmod 754 /usr/local/bin/alertdb-*.sh
USER postgres

CMD ["blackduck-alert-db-cmd.sh","run-postgresql"]