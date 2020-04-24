#!/usr/bin/env bash
dockerSecretDir=/run/secrets
alertDatabaseHost="${ALERT_DB_HOST:-alertdb}"
alertDatabasePort="${ALERT_DB_PORT:-5432}"
alertDatabaseName="${POSTGRES_DB:-alertdb}"
alertDatabaseUser="$POSTGRES_USER"
alertDatabasePassword="$POSTGRES_PASSWORD"

if [ -e $dockerSecretDir/ALERT_DB_USERNAME ];
then
  alertDatabaseUser=$(cat $dockerSecretDir/ALERT_DB_USERNAME)
fi

if [ -e $dockerSecretDir/ALERT_DB_PASSWORD ];
then
  alertDatabasePassword=$(cat $dockerSecretDir/ALERT_DB_PASSWORD)
fi

alertDatabaseConfig="host=$alertDatabaseHost port=$alertDatabasePort dbname=$alertDatabaseName user=$alertDatabaseUser password=$alertDatabasePassword"

if psql "${alertDatabaseConfig}" -c '\l' |grep -q "$alertDatabaseName";
then
    exit 0
else
    exit 1
fi
