# The ARG for the FROM image comes from Gradle. It is based off of postgresContainerVersion,
# and used in buildSrc/docker.gradle
ARG POSTGRESIMAGEVERSION_MIGRATION=""
ARG POSTGRESIMAGEVERSION=""

FROM ${POSTGRESIMAGEVERSION_MIGRATION} AS old

FROM ${POSTGRESIMAGEVERSION}

COPY --from=old /usr/local /usr/local-old

ENV PGBINOLD /usr/local-old/bin
ENV PGBINNEW /usr/local/bin

ARG VERSION
ARG COMMITHASH

LABEL com.blackducksoftware.integration.alert.vendor="Black Duck Software, Inc." \
      com.blackducksoftware.integration.alert.version="$VERSION" \
      com.blackducksoftware.integration.alert.commitHash="$COMMITHASH"

RUN set -ex \
    && mkdir -p -m 775 /var/lib/pgsql \
    && apk update \
    && apk add --no-cache --virtual .run-deps gettext

COPY alertdb-*.sh /usr/local/bin/
COPY openshift-custom-postgresql.conf.template /var/lib/pgsql/

CMD ["postgres"]
ENTRYPOINT ["/usr/local/bin/alertdb-docker-entrypoint.sh"]