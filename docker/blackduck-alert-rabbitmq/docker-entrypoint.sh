#! /bin/sh

logIt() {
  echo "$(date "+%F %T") :: ${1}"
}

logIt "Launching ${0}"

targetRabbitMqHost="${ALERT_RABBIT_MQ_HOST:-rabbitmq}"
blackduckRabbitMqServerCertName=${APPLICATION_NAME}-server
erlangCookieFile="/var/lib/rabbitmq/.erlang.cookie"
clusterSize=${ALERT_RABBIT_CLUSTER_SIZE:-1}
clusterType=${ALERT_RABBIT_CLUSTER_TYPE:-compose}
dockerSecretDir=${RUN_SECRETS_DIR:-/run/secrets}
isSsl=${ALERT_RABBIT_SSL:-false}

if [ -e $dockerSecretDir/ALERT_RABBIT_USER ];
then
  logIt "RabbitMQ user secret set; using value from secret."
  export ALERT_RABBIT_USER=$(cat $dockerSecretDir/ALERT_RABBIT_USER | xargs echo)
fi

if [ -e $dockerSecretDir/ALERT_RABBIT_PASSWORD ];
then
  logIt "RabbitMQ password secret set; using value from secret."
  export ALERT_RABBIT_PASSWORD=$(cat $dockerSecretDir/ALERT_RABBIT_PASSWORD | xargs echo)
fi

manageSelfSignedServerCertificate() {
    logIt "Attempting to generate ${APPLICATION_NAME} self-signed server certificate and key."
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh server-cert \
        --ca "$targetCAHost":"$targetCAPort" \
        --rootcert ${SECURITY_DIR}/root.crt \
        --key ${SECURITY_DIR}/$blackduckRabbitMqServerCertName.key \
        --cert ${SECURITY_DIR}/$blackduckRabbitMqServerCertName.crt \
        --outputDirectory ${SECURITY_DIR} \
        --commonName $blackduckRabbitMqServerCertName \
        --san $targetRabbitMqHost \
        --hostName $targetRabbitMqHost
    exitCode=$?
    if [ $exitCode -eq 0 ];
    then
      logIt "Generated ${APPLICATION_NAME} self-signed server certificate and key."
      chmod 644 ${SECURITY_DIR}/root.crt
      chmod 400 ${SECURITY_DIR}/$blackduckRabbitMqServerCertName.key
      chmod 644 ${SECURITY_DIR}/$blackduckRabbitMqServerCertName.crt
    else
      logIt "ERROR: Unable to generate ${APPLICATION_NAME} self-signed server certificate and key (Code: $exitCode)."
      exit $exitCode
    fi
}

export RABBITMQ_LOGS="${APPLICATION_HOME}/logs/rabbitmq.log"
export RABBITMQ_SASL_LOGS="${APPLICATION_HOME}/logs/rabbitmq-sasl.log"

logIt "
=================================
RabbitMQ Container Configuration:
  SSL enabled:          ${isSsl}
  Clustering Info:
    Cluster Type:       ${clusterType}
    Cluster Size:       ${clusterSize}
  Logs:
    RabbitMQ Logs:      ${RABBITMQ_LOGS}
    RabbitMQ SASL Logs: ${RABBITMQ_SASL_LOGS}
================================="

logIt "Configuring RabbitMQ server..."

logIt "Generating configuration file."
"${APPLICATION_HOME}"/bin/generate-config.sh

if [ -n "${RABBITMQ_ERLANG_COOKIE}" ] ; then
  logIt "Configuring erlang cookie for clustering"
  rm -f "${erlangCookieFile}"
  echo "${RABBITMQ_ERLANG_COOKIE}" > "${erlangCookieFile}"
  chmod 400 "${erlangCookieFile}"
fi

logIt "Enabling plugins."
rabbitmq-plugins enable rabbitmq_management rabbitmq_prometheus rabbitmq_consistent_hash_exchange

if [ "${clusterType}" = 'kubernetes' ];  then
  logIt "Configuring RabbitMQ clustering plugins"
  rabbitmq-plugins enable rabbitmq_peer_discovery_k8s
fi

if [ "${isSsl}" = 'true' ] ; then
  logIt "Configuring SSL for rabbitmq"
  targetCAHost="${HUB_CFSSL_HOST:-cfssl}"
  targetCAPort="${HUB_CFSSL_PORT:-8888}"

  export RABBITMQ_CTL_ERL_ARGS="-proto_dist inet_tls"

  manageSelfSignedServerCertificate

  export RABBITMQ_SSL_CACERTFILE="${SECURITY_DIR}/root.crt"
  export RABBITMQ_SSL_CERTFILE="${SECURITY_DIR}/$blackduckRabbitMqServerCertName.crt"
  export RABBITMQ_SSL_KEYFILE="${SECURITY_DIR}/$blackduckRabbitMqServerCertName.key"
fi

logIt "Start rabbitmq server..."
exec "$@" rabbitmq-server
