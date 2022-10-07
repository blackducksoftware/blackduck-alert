#!/bin/bash
# shellcheck disable=SC2155

imageStartUpCmd="${1}"
dockerSecretDirectory="${RUN_SECRETS_DIR:-/run/secrets}"

function logIt() {
  echo "$(date "+%F %T") :: ${1}"
}

function setValueFromEnvironment() {
  variableName="${1}"
  local secretFile="${2}"
  local envVarName="${3}"
  local envVarValue=$(printenv "${envVarName}")
  local defaultVarName="${4}"
  local defaultVarValue=$(printenv "${defaultVarName}")

  if [ -s "${secretFile}" ]; then
    logIt "${variableName} set from contents of ${secretFile}"
    eval "export ${variableName}=$(< "${secretFile}")"
  elif [ -n "${envVarValue}" ]; then
    logIt "${variableName} already set in environment"
  elif [ -n "${defaultVarValue}" ]; then
    logIt "${variableName} set with value from ENV-VAR ${defaultVarName}"
    eval "export ${variableName}=${defaultVarValue}"
  else
    echo "Unable to set ${variableName}"
    exit 1
  fi
}

if [ -z "${imageStartUpCmd}" ]; then
  logIt "Must supply command to run after processing PG variables"
  exit 1
fi

## Set PGSQL environment variables ##
setValueFromEnvironment POSTGRESQL_USER "${dockerSecretDirectory}/ALERT_DB_USERNAME" POSTGRESQL_USER ALERT_DB_USERNAME
setValueFromEnvironment POSTGRESQL_PASSWORD "${dockerSecretDirectory}/ALERT_DB_PASSWORD" POSTGRESQL_PASSWORD ALERT_DB_PASSWORD
setValueFromEnvironment POSTGRESQL_DATABASE "${dockerSecretDirectory}/ALERT_DB_NAME" POSTGRESQL_DATABASE ALERT_DB_NAME

## Run Image start CMD ##
eval "${imageStartUpCmd}"