#!/usr/bin/env bash
dockerSecretDir=/run/secrets
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

alertDatabaseConfig="host=alertdb port=5432 dbname=alertdb user=$alertDatabaseUser password=$alertDatabasePassword"

if psql "${alertDatabaseConfig}" -c '\l' |grep -q 'alertdb';
then
    exit 0
else
    exit 1
fi
