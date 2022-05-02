FROM blackducksoftware/hub-docker-common:1.0.6 as docker-common
FROM alpine:3.15

ARG VERSION

LABEL com.blackducksoftware.integration.alert.vendor="Black Duck Software, Inc." \
      com.blackducksoftware.integration.alert.version="$VERSION"

ENV ALERT_HOME /opt/blackduck/alert
ENV ALERT_TAR_HOME $ALERT_HOME/alert-tar
ENV ALERT_IMAGES_DIR $ALERT_TAR_HOME/images
ENV ALERT_CONFIG_HOME $ALERT_HOME/alert-config
ENV ALERT_DATA_DIR $ALERT_CONFIG_HOME/data
ENV ALERT_MAX_HEAP_SIZE 2048m

ENV JAVA_HOME /opt/java/openjdk
ENV PATH $JAVA_HOME/bin:$ALERT_TAR_HOME/bin:$PATH
ENV CERTIFICATE_MANAGER_DIR $ALERT_HOME/bin
ENV SECURITY_DIR $ALERT_HOME/security
ENV DEFAULT_BLACKDUCK_ALERT_OPTS -Djava.security.properties=${SECURITY_DIR}/java.security
ENV APPLICATION_NAME blackduck-alert

RUN set -ex \
    && mkdir -p -m 775 $ALERT_CONFIG_HOME $ALERT_TAR_HOME $ALERT_DATA_DIR  $CERTIFICATE_MANAGER_DIR \
    && mkdir -p -m 777 $SECURITY_DIR \
    && apk add --no-cache --virtual .blackduck-alert-run-deps \
           curl \
           jq \
           openssl \
           bash \
           postgresql-client

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
COPY --from=docker-common healthcheck.sh /usr/local/bin/docker-healthcheck.sh
COPY --from=docker-common certificate-manager.sh "$CERTIFICATE_MANAGER_DIR/certificate-manager.sh"
COPY --from=docker-common java.security "$SECURITY_DIR/java.security"

COPY --from=eclipse-temurin:11-jdk-alpine $JAVA_HOME $JAVA_HOME

COPY blackduck-alert-boot-$VERSION $ALERT_TAR_HOME

ENTRYPOINT [ "docker-entrypoint.sh", "blackduck-alert" ]
