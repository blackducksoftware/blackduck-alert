# The ARG's for the FROM images come from Gradle. Base image variables are set in the root build.gradle,
# and used in buildSrc/docker.gradle
ARG BASEIMAGE_BLACKDUCK_ALERT_DB_MIGRATION="default"
ARG BASEIMAGE_BLACKDUCK_ALERT_DB="default"

FROM ${BASEIMAGE_BLACKDUCK_ALERT_DB_MIGRATION} AS old

FROM ${BASEIMAGE_BLACKDUCK_ALERT_DB}

ARG VERSION
ARG COMMITHASH
ARG POSTGRES_MIGRATION_VERSION

COPY --from=old /usr/local /usr/local-old

ENV PGBINOLD=/usr/local-old/bin
ENV PGBINNEW=/usr/local/bin
ENV POSTGRES_MIGRATION_VERSION=${POSTGRES_MIGRATION_VERSION}

LABEL com.blackducksoftware.integration.alert.vendor="Black Duck Software, Inc." \
      com.blackducksoftware.integration.alert.version="$VERSION" \
      com.blackducksoftware.integration.alert.commitHash="$COMMITHASH"

RUN set -ex \
    && mkdir -p -m 775 /var/lib/pgsql

COPY alertdb-*.sh /usr/local/bin/

CMD ["postgres"]
ENTRYPOINT ["/usr/local/bin/alertdb-docker-entrypoint.sh"]