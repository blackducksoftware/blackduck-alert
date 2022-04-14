#! /bin/sh

targetRabbitMqHost="${RABBIT_MQ_HOST:-rabbitmq}"
blackduckRabbitmqDir="$APPLICATION_HOME"
blackduckRabbitMqServerCertName=$APPLICATION_NAME-server
rabbitmqInstall=/var/lib/rabbitmq
rabbitmqSettings=/etc/rabbitmq

isSsl=${BLACKDUCK_RABBIT_SSL:-true}

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

echo "Configuring RabbitMQ server... "

rabbitmq-plugins enable rabbitmq_management
rabbitmq-plugins enable rabbitmq_prometheus
rabbitmq-plugins enable rabbitmq_consistent_hash_exchange
echo "SSL enabled: ${isSsl}"
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

exec "$@" /usr/local/bin/docker-entrypoint.sh rabbitmq-server
