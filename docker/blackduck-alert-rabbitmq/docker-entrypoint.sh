#! /bin/sh

targetRabbitMqHost="${ALERT_RABBIT_MQ_HOST:-rabbitmq}"
blackduckRabbitmqDir="$APPLICATION_HOME"
blackduckRabbitMqServerCertName=$APPLICATION_NAME-server
rabbitmqInstall=/var/lib/rabbitmq
rabbitmqSettings=/etc/rabbitmq
clusterSize=${ALERT_RABBIT_CLUSTER_SIZE:-1}
clusterType=${ALERT_RABBIT_CLUSTER_TYPE:-compose}

isSsl=${ALERT_RABBIT_SSL:-false}

if [ -e $dockerSecretDir/ALERT_RABBIT_USER ];
then
  echo "RabbitMQ user secret set; using value from secret."
  export ALERT_RABBIT_USER=$(cat $dockerSecretDir/ALERT_RABBIT_USER | xargs echo)
fi

if [ -e $dockerSecretDir/ALERT_RABBIT_PASSWORD ];
then
  echo "RabbitMQ password secret set; using value from secret."
  export ALERT_RABBIT_PASSWORD=$(cat $dockerSecretDir/ALERT_RABBIT_PASSWORD | xargs echo)
fi

manageSelfSignedServerCertificate() {
    echo "Attempting to generate $APPLICATION_NAME self-signed server certificate and key."
    $CERTIFICATE_MANAGER_DIR/certificate-manager.sh server-cert \
        --ca "$targetCAHost":"$targetCAPort" \
        --rootcert $SECURITY_DIR/root.crt \
        --key $SECURITY_DIR/$blackduckRabbitMqServerCertName.key \
        --cert $SECURITY_DIR/$blackduckRabbitMqServerCertName.crt \
        --outputDirectory $SECURITY_DIR \
        --commonName $blackduckRabbitMqServerCertName \
        --san $targetRabbitMqHost \
        --hostName $targetRabbitMqHost
    exitCode=$?
    if [ $exitCode -eq 0 ];
    then
      echo "Generated $APPLICATION_NAME self-signed server certificate and key."
      chmod 644 $SECURITY_DIR/root.crt
      chmod 400 $SECURITY_DIR/$blackduckRabbitMqServerCertName.key
      chmod 644 $SECURITY_DIR/$blackduckRabbitMqServerCertName.crt
    else
      echo "ERROR: Unable to generate $APPLICATION_NAME self-signed server certificate and key (Code: $exitCode)."
      exit $exitCode
    fi
}

export RABBITMQ_LOGS="$APPLICATION_HOME/logs/rabbitmq.log"
export RABBITMQ_SASL_LOGS="$APPLICATION_HOME/logs/rabbitmq-sasl.log"
echo "================================="
echo "RabbitMQ Container Configuration:"
echo "  SSL enabled:          ${isSsl}"
echo "  Clustering Info:"
echo "    Cluster Type:       ${clusterType}"
echo "    Cluster Size:       ${clusterSize}"
echo "  Logs:"
echo "    RabbitMQ Logs:      ${RABBITMQ_LOGS}"
echo "    RabbitMQ SASL Logs: ${RABBITMQ_SASL_LOGS}"
echo ""
echo "================================="
echo "Configuring RabbitMQ server... "
echo "Generating configuration file."
"${blackduckRabbitmqDir}"/bin/generate-config.sh

if [ -n "$RABBITMQ_ERLANG_COOKIE" ] ; then
  echo "Configuring erlang cookie for clustering"
  rm -f "${rabbitmqInstall}"/.erlang.cookie
  echo "${RABBITMQ_ERLANG_COOKIE}" > "${rabbitmqInstall}"/.erlang.cookie
  chmod 400 "${rabbitmqInstall}"/.erlang.cookie
fi

rabbitmq-plugins enable rabbitmq_management
rabbitmq-plugins enable rabbitmq_prometheus
rabbitmq-plugins enable rabbitmq_consistent_hash_exchange

if [ "${clusterType}" = 'kubernetes' ];  then
  echo "Configuring RabbitMQ clustering plugins"
  rabbitmq-plugins enable rabbitmq_peer_discovery_k8s
fi


if [ "$isSsl" = 'true' ] ; then
  echo "Configuring SSL for rabbitmq"
  targetCAHost="${HUB_CFSSL_HOST:-cfssl}"
  targetCAPort="${HUB_CFSSL_PORT:-8888}"

  export RABBITMQ_CTL_ERL_ARGS=-proto_dist inet_tls

  manageSelfSignedServerCertificate

  export RABBITMQ_SSL_CACERTFILE="$blackduckRabbitmqDir"/security/root.crt
  export RABBITMQ_SSL_CERTFILE="$blackduckRabbitmqDir"/security/"$blackduckRabbitMqServerCertName".crt
  export RABBITMQ_SSL_KEYFILE="$blackduckRabbitmqDir"/security/"$blackduckRabbitMqServerCertName".key
fi
echo "Start rabbitmq server..."

exec "$@" rabbitmq-server
