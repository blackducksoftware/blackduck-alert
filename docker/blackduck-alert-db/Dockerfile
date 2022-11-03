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

LABEL com.blackducksoftware.integration.alert.vendor="Black Duck Software, Inc." \
      com.blackducksoftware.integration.alert.version="$VERSION"

COPY alertdb-*.sh /usr/local/bin/

CMD ["postgres"]
ENTRYPOINT ["/usr/local/bin/alertdb-docker-entrypoint.sh"]