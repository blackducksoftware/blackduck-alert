FROM openjdk:8-jre-alpine

ARG VERSION

LABEL com.blackducksoftware.integration.alert.vendor="Black Duck Software, Inc." \
      com.blackducksoftware.integration.alert.version="$VERSION"

ENV ALERT_HOME /opt/blackduck/alert/alert-tar/hub-alert-$VERSION
ENV PATH $ALERT_HOME/bin:$PATH
ENV ALERT_DB_HOME /opt/blackduck/alert/alert-db-volume

RUN set -e \
    # The old version of the Gradle Application plugin generates Bash scripts
    && apk add --no-cache --virtual bash \
    && addgroup -S hubalert \
    && adduser -h "$ALERT_HOME" -g hubalert -s /sbin/nologin -G hubalert -S -D -H hubalert

ADD "build/distributions/hub-alert-$VERSION.tar" /opt/blackduck/alert-tar/

# Override the default logger settings to match other Hub containers

RUN mkdir -p /opt/blackduck/alert-config-defaults
RUN chown -R hubalert:hubalert /opt/blackduck/alert-tar/
RUN chown -R hubalert:hubalert /opt/blackduck/alert-config-defaults/

VOLUME [ "/opt/blackduck/alert-config-volume/" ]

# The app itself will read in from the -volume directory at runtime.  We write these to an
# easily accessible location that the entrypoint can always find and copy data from.
RUN cp -r /opt/blackduck/alert-tar/alert-config-defaults/* /opt/blackduck/alert-config-defaults/


COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080

ENTRYPOINT [ "docker-entrypoint.sh" ]

CMD [ "hub-alert" ]
