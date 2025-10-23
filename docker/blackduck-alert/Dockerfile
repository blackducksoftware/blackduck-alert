# The ARG's for the FROM images come from Gradle. Base image variables are set in the root build.gradle,
# and used in buildSrc/docker.gradle
ARG BASEIMAGE_HUB_DOCKER_COMMON="default"
ARG BASEIMAGE_BLACKDUCK_ALERT="default"

FROM ${BASEIMAGE_HUB_DOCKER_COMMON} AS docker-common
FROM ${BASEIMAGE_BLACKDUCK_ALERT}

ARG VERSION
ARG COMMITHASH

LABEL com.blackducksoftware.integration.alert.vendor="Black Duck Software, Inc." \
      com.blackducksoftware.integration.alert.version="$VERSION" \
      com.blackducksoftware.integration.alert.commitHash="$COMMITHASH"

ENV ALERT_HOME=/opt/blackduck/alert
ENV ALERT_TAR_HOME=$ALERT_HOME/alert-tar
ENV ALERT_IMAGES_DIR=$ALERT_TAR_HOME/images
ENV ALERT_CONFIG_HOME=$ALERT_HOME/alert-config
ENV ALERT_DATA_DIR=$ALERT_CONFIG_HOME/data
ENV ALERT_MAX_HEAP_SIZE=2048m

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk
ENV PATH=$JAVA_HOME/bin:$ALERT_TAR_HOME/bin:$PATH
ENV CERTIFICATE_MANAGER_DIR=$ALERT_HOME/bin
ENV SECURITY_DIR=$ALERT_HOME/security
ENV DEFAULT_BLACKDUCK_ALERT_OPTS=-Djava.security.properties=${SECURITY_DIR}/java.security
ENV APPLICATION_NAME=blackduck-alert

RUN set -ex \
    && mkdir -p -m 775 $ALERT_CONFIG_HOME $ALERT_TAR_HOME $ALERT_DATA_DIR $CERTIFICATE_MANAGER_DIR \
    && mkdir -p -m 777 $SECURITY_DIR \
    && apk update \
    && apk add --no-cache --virtual .blackduck-alert-run-deps \
           openjdk17 \
           curl \
           jq \
           openssl \
           bash \
           postgresql-client

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
COPY --from=docker-common healthcheck.sh /usr/local/bin/docker-healthcheck.sh
COPY --from=docker-common certificate-manager.sh "$CERTIFICATE_MANAGER_DIR/certificate-manager.sh"
COPY --from=docker-common java.security "$SECURITY_DIR/java.security"

COPY blackduck-alert-boot-$VERSION $ALERT_TAR_HOME

ENTRYPOINT [ "docker-entrypoint.sh", "blackduck-alert" ]
