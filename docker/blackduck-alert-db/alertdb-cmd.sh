#!/bin/bash
# shellcheck disable=SC2155

imageStartUpCmd="${1}"
dockerSecretDirectory="${RUN_SECRETS_DIR:-/run/secrets}"

function logIt() {
  echo "$(date "+%F %T") :: ${1}"
}

function setValueFromEnvironment() {
  postgresqlVariableName="${1}"
  local alertVariableName="${2}"

  local secretFile="${dockerSecretDirectory}/${alertVariableName}"

  local postgresVariableValue=$(printenv "${postgresqlVariableName}")
  local alertVariableValue=$(printenv "${alertVariableName}")

  if [ -s "${secretFile}" ]; then
    logIt "${postgresqlVariableName} set from contents of ${secretFile}"
    eval "export ${postgresqlVariableName}=$(< "${secretFile}")"
  elif [ -n "${postgresVariableValue}" ]; then
    logIt "${postgresqlVariableName} already set in environment"
  elif [ -n "${alertVariableValue}" ]; then
    logIt "${postgresqlVariableName} set with value from ENV-VAR ${alertVariableName}"
    eval "export ${postgresqlVariableName}=${alertVariableValue}"
  else
    echo "Unable to set ${postgresqlVariableName}"
    exit 1
  fi
}

if [ -z "${imageStartUpCmd}" ]; then
  logIt "Must supply command to run after processing PG variables"
  exit 1
fi

## Set PGSQL environment variables ##
setValueFromEnvironment POSTGRESQL_USER ALERT_DB_USERNAME
setValueFromEnvironment POSTGRESQL_PASSWORD ALERT_DB_PASSWORD
setValueFromEnvironment POSTGRESQL_DATABASE ALERT_DB_NAME

## Run Image start CMD ##
eval "${imageStartUpCmd}"