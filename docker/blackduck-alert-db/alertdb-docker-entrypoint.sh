#!/bin/bash
# shellcheck disable=SC2112,SC2155

if [ -n "${ALERT_SCRIPT_DEBUG}" ] && [ "true" = "${ALERT_SCRIPT_DEBUG}" ] ; then
  set -x
fi

function _logIt() {
  echo "$(date "+%F %T") :: ${1}"
}

function _logStart() {
  _logIt "Function start: '${FUNCNAME[1]}' from script: '$(basename "${BASH_SOURCE[-1]}")'"
}

function _logEnd() {
  _logIt "Function end:   '${FUNCNAME[1]}' from script: '$(basename "${BASH_SOURCE[-1]}")'"
}

function _checkStatus() {
  if [ "${1}" -ne 0 ]; then
    _logIt "ERROR: ${2} (Exit Code: ${1})."
    exit "${1}"
  else
    _logIt "Successfully ran: ${2}"
  fi
}

function _validate_environment() {
  _logStart
  if [ -z "${osUser}" ]; then
    _checkStatus 1 "Script requires one argument of OS user to run commands as (usually 'postgres')"
  fi
  if [ -z "${builtInEntrypointScript}" ]; then
    _checkStatus 1 "Unable to identify built in entry point script on PATH (${builtInEntrypointScriptName})"
  fi
  if [ ! -s "${alertDBMigrationScript}" ] || [ ! -x "${alertDBMigrationScript}" ];  then
    _checkStatus 1 "Unable to find DB migration script: ${alertDBMigrationScript}"
  fi
  _logEnd
}

function _set_values_in_environment() {
  _logStart
  local environmentVariableName="${1}"
  local alertFileSecret="${2}"
  local postgresFile="${3}"
  local postgresEnvVar="${4}"
  local defaultValue="${5}"
  local value=""
  local source=""

  if [ -n "${alertFileSecret}" ] && [ -s "${alertFileSecret}" ]; then
    value=$(<"${alertFileSecret}")
    source="Alert Secret"
  elif [ -n "${postgresFile}" ] && [ -s "${postgresFile}" ]; then
    _logIt "Postgres File is set. Not setting ${environmentVariableName}"
    _logEnd
    return
  elif [ -n "${postgresEnvVar}" ]; then
    value="${postgresEnvVar}"
    source="Postgres Environment Variable"
  elif [ -n "${defaultValue}" ]; then
    value="${defaultValue}"
    source="Script Default"
  fi

  if [ -z "${value}" ]; then
    _checkStatus 1 "Unable to set ${environmentVariableName}. Priority order is: Alert Secret, Postgres File, Postgres environment variable, default listed in ${BASH_SOURCE:-$0}"
  fi

  eval "export ${environmentVariableName}=\"${value}\""
  _checkStatus $? "Setting ${environmentVariableName} from ${source}"
  _logEnd
}

function _run_DB_migration() {
  _logStart
  "${alertDBMigrationScript}" "${osUser}"
  _checkStatus $? "Running: ${alertDBMigrationScript}"
  _logEnd
}

function _launch_postgres() {
  _logStart
  _logIt "Launching built in entrypoint script: ${builtInEntrypointScript}"
  echo ""
  "${builtInEntrypointScript}" "${osUser}"
  _logEnd
}

############## START ##############
_logIt "Launching ${0}"

osUser="${1}"

dockerSecretDir=${RUN_SECRETS_DIR:-/run/secrets}

runDirectory=$(dirname "${0}")
alertDBMigrationScript="${runDirectory}"/alertdb-pg-migrate.sh

builtInEntrypointScriptName="docker-entrypoint.sh"
builtInEntrypointScript=$(which ${builtInEntrypointScriptName})

set -e

_validate_environment

_set_values_in_environment POSTGRES_USER "${dockerSecretDir}/ALERT_DB_USERNAME" "${POSTGRES_USER_FILE}" "${POSTGRES_USER}" "sa"
_set_values_in_environment POSTGRES_PASSWORD "${dockerSecretDir}/ALERT_DB_PASSWORD" "${POSTGRES_PASSWORD_FILE}" "${POSTGRES_PASSWORD}" "blackduck"
_set_values_in_environment POSTGRES_DB "${dockerSecretDir}/POSTGRES_DB" "" "${POSTGRES_DB}" "alertdb"

_run_DB_migration

_launch_postgres