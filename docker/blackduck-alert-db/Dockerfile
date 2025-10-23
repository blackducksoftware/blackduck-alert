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
ENV ICU74=/opt/icu74

LABEL com.blackducksoftware.integration.alert.vendor="Black Duck Software, Inc." \
      com.blackducksoftware.integration.alert.version="$VERSION" \
      com.blackducksoftware.integration.alert.commitHash="$COMMITHASH"

RUN set -ex \
    && mkdir -p -m 775 /var/lib/pgsql \
    && apk update \
    && apk add --no-cache --virtual .run-deps gettext

## Install ICU74 to support upgrading from PG-15
RUN set -ex \
    && apk add build-base wget tar \
    && mkdir -p /tmp/icu74-source \
    && cd /tmp/icu74-source \
    && wget https://github.com/unicode-org/icu/releases/download/release-74-2/icu4c-74_2-src.tgz \
    && tar -xzf icu4c-74_2-src.tgz \
    && cd icu/source \
    && ./configure --prefix=/opt/icu74 \
    && make -j$(nproc) \
    && make install \
    && cd /tmp \
    && rm -rf /tmp/icu74-source

COPY alertdb-*.sh /usr/local/bin/
COPY openshift-custom-postgresql.conf.template /var/lib/pgsql/

CMD ["postgres"]
ENTRYPOINT ["/usr/local/bin/alertdb-docker-entrypoint.sh"]