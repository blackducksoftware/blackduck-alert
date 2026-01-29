#! /bin/sh

rabbitHome=/etc/rabbitmq
blackduckSecurityHome=/opt/blackduck/rabbitmq/security
confFile="${rabbitHome}/rabbitmq.conf"
envFile="${rabbitHome}/rabbitmq-env.conf"
isSsl=${ALERT_RABBITMQ_SSL:-false}
clusterSize=${ALERT_RABBITMQ_CLUSTER_SIZE:-1}
clusterType=${ALERT_RABBITMQ_CLUSTER_TYPE:-compose}

echo "Generating $envFile ..."
echo "SERVER_ADDITIONAL_ERL_ARGS=\"+A ${ALERT_RABBITMQ_ERLANG_ASYNC_THREADS:-4}\"" >> $envFile

echo "Generating $confFile ..."

cat >> ${confFile} << EOL
loopback_users.guest = false
log.console = true
log.console.level = warning
default_user = ${ALERT_RABBITMQ_USER:-sysadmin}
default_pass = ${ALERT_RABBITMQ_PASSWORD:-blackduck}
default_vhost = blackduck-alert
queue_master_locator=min-masters
prometheus.return_per_object_metrics = true
consumer_timeout = 21600000
background_gc_enabled = true
collect_statistics_interval = 15000
EOL

# connection settings
echo "credit_flow_default_credit = ${ALERT_RABBITMQ_CREDIT_FLOW:-400}" >> $confFile
echo "channel_max = ${ALERT_RABBITMQ_CONNECTION_CHANNEL_MAX:-512}" >> $confFile
echo "num_acceptors.tcp = ${ALERT_RABBITMQ_CONNECTION_TCP_ACCEPTORS:-10}" >> $confFile
echo "heartbeat = ${ALERT_RABBITMQ_CONNECTION_HEARTBEAT:-60}" >> $confFile

# memory settings
echo "total_memory_available_override_value = ${ALERT_RABBITMQ_MEMORY_HIGH_WATERMARK:-1GB}" >> $confFile
echo "vm_memory_high_watermark.absolute = ${ALERT_RABBITMQ_MEMORY_HIGH_WATERMARK:-1GB}" >> $confFile
# Set the amount of memory rabbit can use to 70% of system, overridable by the property below
echo "vm_memory_high_watermark.relative = ${ALERT_RABBITMQ_MEMORY_WATERMARK:-0.70}" >> $confFile

if [ "$isSsl" = 'true' ] ; then
  cat  >> ${confFile} << EOL
listeners.ssl.default = ${ALERT_RABBITMQ_PORT:-5671}
ssl_options.cacertfile = "${blackduckSecurityHome}/root.crt"
ssl_options.certfile = "${blackduckSecurityHome}/rabbitmq-server.crt"
ssl_options.fail_if_no_peer_cert = false
ssl_options.keyfile = "${blackduckSecurityHome}/rabbitmq-server.key"
ssl_options.verify = verify_peer
management.ssl.port = ${ALERT_RABBITMQ_MGMNT_PORT:-15671}
EOL
else
  cat >> ${confFile} << EOL
listeners.tcp.default = ${ALERT_RABBITMQ_PORT:-5672}
management.tcp.port = ${ALERT_RABBITMQ_MGMNT_PORT:-15672}
EOL
fi

if [ "${clusterType}" = 'kubernetes' ] ; then
  clusterName=${ALERT_RABBITMQ_CLUSTER_NAME:-cluster.local}
  cat  >> ${confFile} << EOL
cluster_formation.peer_discovery_backend  = rabbit_peer_discovery_k8s
cluster_formation.k8s.host = kubernetes.default.svc.${clusterName}
cluster_formation.k8s.address_type = hostname
cluster_formation.node_cleanup.interval = 30
cluster_formation.node_cleanup.only_log_warning = true
cluster_partition_handling = autoheal
EOL
elif [ "$clusterSize" -gt 1 ] ; then
    echo "# RabbitMQ Compose/Swarm cluster" >> $confFile
    echo "cluster_formation.peer_discovery_backend = rabbit_peer_discovery_classic_config" >> $confFile

    for i in $(seq 1 "$clusterSize") ; do
      echo "cluster_formation.classic_config.nodes.${i} = rabbit@rabbitmq${i}" >> $confFile
    done
fi

# Default image disables management statistics, but we want them on
echo management_agent.disable_metrics_collector = false > /etc/rabbitmq/conf.d/management_agent.disable_metrics_collector.conf
