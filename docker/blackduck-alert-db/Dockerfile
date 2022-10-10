# The ARG for the FROM image comes from Gradle. It is based off of postgresContainerVersion,
# and used in buildSrc/docker.gradle
ARG POSTGRESBASEIMAGE=""
FROM ${POSTGRESBASEIMAGE}

ARG VERSION

LABEL com.blackducksoftware.integration.alert.vendor="Black Duck Software, Inc." \
      com.blackducksoftware.integration.alert.version="$VERSION"

COPY alertdb-healthcheck.sh /usr/local/bin/alertdb-healthcheck.sh
COPY alertdb-cmd.sh /usr/local/bin/alertdb-cmd.sh

CMD ["alertdb-cmd.sh","run-postgresql"]