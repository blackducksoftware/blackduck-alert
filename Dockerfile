FROM blackducksoftware/hub-docker-common:1.0.2 as docker-common
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
ENV ALERT_TAR_HOME $ALERT_HOME/alert-tar
ENV PATH $ALERT_TAR_HOME/bin:$PATH
ENV ALERT_DATA_DIR $ALERT_CONFIG_HOME/data
ENV ALERT_DB_DIR $ALERT_DATA_DIR/alertdb
ENV ALERT_TEMPLATES_DIR $ALERT_TAR_HOME/templates
ENV ALERT_IMAGES_DIR $ALERT_TAR_HOME/images

ENV ALERT_MAX_HEAP_SIZE 350m

RUN set -e \
    && mkdir -p $ALERT_HOME \
    && apk add --no-cache --virtual .blackduck-alert-run-deps \
    		curl \
    		jq \
    		openssl \
    		bash \
    && addgroup -S alert \
    && adduser -h "$ALERT_HOME" -g alert -s /sbin/nologin -G alert -S -D alert

COPY --chown=alert:alert blackduck-alert-boot-$VERSION $ALERT_HOME/alert-tar

COPY --chown=alert:alert docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
COPY --chown=alert:alert --from=docker-common healthcheck.sh /usr/local/bin/docker-healthcheck.sh
COPY --chown=alert:alert --from=docker-common certificate-manager.sh "$CERTIFICATE_MANAGER_DIR/certificate-manager.sh"

USER alert:alert
WORKDIR $ALERT_HOME

RUN set -e \
    && mkdir -p $SECURITY_DIR && mkdir -p $ALERT_DB_DIR \
    && chmod 775 $SECURITY_DIR && chmod 775 $ALERT_DB_DIR

EXPOSE 8080

ENTRYPOINT [ "docker-entrypoint.sh" ]

CMD [ "blackduck-alert" ]
