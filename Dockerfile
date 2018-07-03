FROM blackducksoftware/hub-docker-common:1.0.0 as docker-common
FROM openjdk:8-jre-alpine

ARG VERSION

LABEL com.blackducksoftware.integration.alert.vendor="Black Duck Software, Inc." \
      com.blackducksoftware.integration.alert.version="$VERSION"
      
ENV APPLICATION_NAME blackduck-alert
ENV BLACKDUCK_HOME /opt/blackduck
ENV CERTIFICATE_MANAGER_DIR $BLACKDUCK_HOME/alert/bin

ENV ALERT_HOME $BLACKDUCK_HOME/alert
ENV ALERT_CONFIG_HOME $ALERT_HOME/alert-config
ENV SECURITY_DIR $ALERT_CONFIG_HOME/security
ENV ALERT_TAR_HOME $ALERT_HOME/alert-tar/blackduck-alert-boot-$VERSION
ENV PATH $ALERT_TAR_HOME/bin:$PATH
ENV ALERT_DB_DIR $ALERT_CONFIG_HOME/data/alertdb
ENV ALERT_TEMPLATES_DIR $ALERT_TAR_HOME/templates
ENV ALERT_IMAGES_DIR $ALERT_TAR_HOME/images

ENV ALERT_MAX_HEAP_SIZE 350m

RUN set -e \
    # The old version of the Gradle Application plugin generates Bash scripts
    && apk add --no-cache --virtual .blackduck-alert-run-deps \
    		curl \
    		jq \
    		openssl \
    		bash \
    && addgroup -S alert \
    && adduser -h "$ALERT_HOME" -g alert -s /sbin/nologin -G alert -S -D -H alert

ADD "build/distributions/blackduck-alert-boot-$VERSION.tar" /opt/blackduck/alert/alert-tar/

# Override the default logger settings to match other Hub containers

RUN mkdir -p $CERTIFICATE_MANAGER_DIR
RUN mkdir -p $ALERT_CONFIG_HOME
RUN mkdir -p $SECURITY_DIR
RUN mkdir -p $ALERT_DB_DIR

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
COPY --from=docker-common healthcheck.sh /usr/local/bin/docker-healthcheck.sh
COPY --from=docker-common certificate-manager.sh "$CERTIFICATE_MANAGER_DIR/certificate-manager.sh"

RUN chown -R alert:alert $BLACKDUCK_HOME
RUN chmod 774 $ALERT_DB_DIR

RUN chmod +x /usr/local/bin/docker-entrypoint.sh
RUN chmod +x $CERTIFICATE_MANAGER_DIR/certificate-manager.sh

VOLUME [ "$SECURITY_DIR" ]

EXPOSE 8080

ENTRYPOINT [ "docker-entrypoint.sh" ]

CMD [ "blackduck-alert" ]
