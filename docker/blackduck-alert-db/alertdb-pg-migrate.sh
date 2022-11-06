#! /bin/bash
# shellcheck disable=SC2155
set -e

function _set_value() {
  _logStart
  local resultVariable="${1}"
  local environmentValue="${2}"
  local localFile="${3}"

  if [ -n "${environmentValue}" ]; then
    eval "${resultVariable}=\"${environmentValue}\""
  elif [ -n "${localFile}" ] && [ -s "${localFile}" ]; then
    eval "export ${resultVariable}=$(<"${localFile}")"
  else
    _checkStatus 1 "Unable to set value from environment"
  fi
  _logEnd
}

function _validate_environment() {
  _logStart
  if [ -z "${osUser}" ]; then
    _checkStatus 1 "Script requires one argument of OS user to run commands as (usually 'postgres')"
  fi
  if [ -z "${PGDATA}" ]; then
    _checkStatus 1 "Verifying environment variable: PGDATA"
  fi
  if [ -z "${PGBINOLD}" ]; then
    _checkStatus 1 "Verifying environment variable: PGBINOLD"
  fi
  if [ -z "${PGBINNEW}" ]; then
    _checkStatus 1 "Verifying environment variable: PGBINNEW"
  fi
  _logEnd
}

function _validate_migration_viability() {
  _logStart
  local pgVersionFile="${PGDATA}/PG_VERSION"
  if [ ! -s "${pgVersionFile}" ]; then
    _logIt "Assume new deployment, ${pgVersionFile} does not exist"
    exit 0
  fi

  local pgVersionFileValue=$(<"${pgVersionFile}")
  local environmentPostgresVersion=$(postgres -V | awk -F' |\\.' '{print$3}')

  if [ "${environmentPostgresVersion}" == "${pgVersionFileValue}" ]; then
    _logIt "Version of data is already ${environmentPostgresVersion}, migration not needed"
    exit 0
  fi

  local supportedMigrationVersion="12"
  if [ "${supportedMigrationVersion}" != "${pgVersionFileValue}" ]; then
    _checkStatus 1 "Invalid Postgres data version. Migration is only supported from ${supportedMigrationVersion}"
  fi

  _logIt "Image is running PG version ${environmentPostgresVersion} and data is from PG version ${pgVersionFileValue}. PG migration needs to run."
  _logEnd
}

function _update_existing_data_ownership() {
  _logStart
  local userId=$(id -g "${osUser}")
  local groupId=$(id -g "${osUser}")
  chown -R "${userId}:${groupId}" "${inputPostgresDataDirectory}"
  _checkStatus $? "Updating ownership for existing PG data"
  _logEnd
}

function _move_existing_data_backup_directory() {
  _logStart
  local existingDirectoryMode=$(stat -c '%a' "${inputPostgresDataDirectory}")
  su-exec "${osUser}" mkdir -m "${existingDirectoryMode}" -p "${backupPostgresDataDirectory}"
  _checkStatus $? "Creating backup directory : ${backupPostgresDataDirectory}"

  # shellcheck disable=SC2046,SC2010
  mv "${inputPostgresDataDirectory}"/* "${backupPostgresDataDirectory}"/.
  _checkStatus $? "Moving input PG data into ${backupPostgresDataDirectory}"
  _logEnd
}

function _initialize_new_db() {
  _logStart
  su "${osUser}" -c "initdb --username=\"${postgresUser}\" --pwfile=<(echo \"${postgresPassword}\")"
  _checkStatus $? "Initializing DB"
  _logEnd
}

function _pg_upgrade() {
  _logStart
  local upgradeCmd="su-exec ${osUser} pg_upgrade -U ${postgresUser} ${1}"
  ${upgradeCmd}
  _checkStatus $? "${upgradeCmd}"
  _logEnd
}

function _start_postgres() {
  _logStart
  su-exec "${osUser}" pg_ctl -m smart -w start
  _checkStatus $? "Start running postgres"
  _logEnd
}

function _analyze_db() {
  _logStart
  vacuumdb -U "${postgresUser}" --analyze-only --all --verbose
  _checkStatus $? "Running vacuumdb --analyze-only"
  _logEnd
}

function _stop_postgres() {
  _logStart
  su-exec "${osUser}" pg_ctl -m smart -w stop
  _checkStatus $? "Stop running postgres"
  _logEnd
}

############## START ##############
_logIt "Launching ${0}"

osUser="${1}"

runDirectory="/tmp"
epochTime="$(date +%s)"

inputPostgresDataDirectory="${PGDATA}"
backupPostgresDataDirectory="${runDirectory}/backup.${epochTime}"

pgHbaConfFileName="pg_hba.conf"

postgresUser=""
postgresPassword=""

## Change directory
cd "${runDirectory}"
_checkStatus $? "Changing directory to ${runDirectory}"

_set_value postgresUser "${POSTGRES_USER}" "${POSTGRES_USER_FILE}"
_set_value postgresPassword "${POSTGRES_PASSWORD}" "${POSTGRES_PASSWORD_FILE}"

## Check run environment
_validate_environment

## Check if we can and should run
_validate_migration_viability

## Prepare existing data for migration
_update_existing_data_ownership
_move_existing_data_backup_directory

## Set values needed for pg_upgrade
export PGDATAOLD="${backupPostgresDataDirectory}"
export PGDATANEW="${inputPostgresDataDirectory}"

## Initialize new DB to be used to upgrade existing PG data
_initialize_new_db

## Perform upgrade
_pg_upgrade "--check"
_pg_upgrade

## Validate upgrade
_start_postgres
_analyze_db
_stop_postgres

## Move hba conf file from OLD to NEW
mv "${PGDATAOLD}/${pgHbaConfFileName}" "${PGDATANEW}"/.
_checkStatus $? "Moving ${pgHbaConfFileName}"

## Cleanup migrated data
rm -rf "${PGDATAOLD}"
_checkStatus $? "Removing ${PGDATAOLD}"

_logIt "Migration complete"
exit 0
