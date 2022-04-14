#! /bin/sh

targetRabbitMqHost="${RABBIT_MQ_HOST:-rabbitmq}"
blackduckRabbitmqDir="$APPLICATION_HOME"
blackduckRabbitMqServerCertName=$APPLICATION_NAME-server
rabbitmqInstall=/var/lib/rabbitmq
rabbitmqSettings=/etc/rabbitmq

isSsl=${BLACKDUCK_RABBIT_SSL:-true}

echo "Configuring RabbitMQ server, SSL: ${isSsl}"

exec "$@" /usr/local/bin/docker-entrypoint.sh rabbitmq-server
