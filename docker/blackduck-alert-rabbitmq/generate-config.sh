#! /bin/sh

rabbitHome=/etc/rabbitmq
blackduckSecurityHome=/opt/blackduck/rabbitmq/security
confFile="${rabbitHome}/rabbitmq.conf"
isSsl=${ALERT_RABBIT_SSL:-false}
clusterSize=${ALERT_RABBIT_CLUSTER_SIZE:-1}
clusterType=${ALERT_RABBIT_CLUSTER_TYPE:-compose}

cat >> ${confFile} << EOL
loopback_users.guest = false
total_memory_available_override_value = 1GB
log.console = true
default_user = ${ALERT_RABBIT_USER:-sysadmin}
default_pass = ${ALERT_RABBIT_PASSWORD:-blackduck}
default_vhost = blackduck-alert
queue_master_locator=min-masters
prometheus.return_per_object_metrics = true
consumer_timeout = 21600000
EOL

if [ "$isSsl" = 'true' ] ; then
  cat  >> ${confFile} << EOL
listeners.ssl.default = 5671
ssl_options.cacertfile = "${blackduckSecurityHome}/root.crt"
ssl_options.certfile = "${blackduckSecurityHome}/rabbitmq-server.crt"
ssl_options.fail_if_no_peer_cert = false
ssl_options.keyfile = "${blackduckSecurityHome}/rabbitmq-server.key"
ssl_options.verify = verify_peer
management.ssl.port = 15671
EOL
fi

if [ "${clusterType}" = 'kubernetes' ] ; then
  clusterName=${ALERT_RABBIT_CLUSTER_NAME:-cluster.local}
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

# Set the amount of memory rabbit can use to 70% of system, overridable by the property below
echo "vm_memory_high_watermark.relative = ${ALERT_RABBITMQ_MEMORY_WATERMARK:-0.70}" >> $confFile

# Default image disables management statistics, but we want them on
echo management_agent.disable_metrics_collector = false > /etc/rabbitmq/conf.d/management_agent.disable_metrics_collector.conf
