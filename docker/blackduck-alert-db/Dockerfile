# The ARG for the FROM image comes from Gradle. It is based off of postgresContainerVersion,
# and used in buildSrc/docker.gradle
ARG POSTGRESIMAGEVERSION=""
FROM ${POSTGRESIMAGEVERSION}

ARG VERSION

LABEL com.blackducksoftware.integration.alert.vendor="Black Duck Software, Inc." \
      com.blackducksoftware.integration.alert.version="$VERSION"

COPY alertdb-healthcheck.sh /usr/local/bin/alertdb-healthcheck.sh