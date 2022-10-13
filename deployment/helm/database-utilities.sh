#!/bin/bash
set -e

#defaults
backup=false
restore=false
file=
databaseKeyword="-postgres-"
databaseName=alertdb
userName=sa
deploymentNamespace=default

# functions
usage() {
  echo "usage: database-utilities - backup or restore a database with kubectl."
  echo
  echo "database-utilities.sh [-b] [-f file] [-d databaseName] [-k containerKeyword] [-n namespace] [-r] [-u userName]"
  echo "Options: "
  echo "  -b: backup a database to the file specified in the file option."
  echo "  -f: the file to save a backup or the file to restore the database from."
  echo "  -d: the name of the database."
  echo "  -k: the keyword to search for the database container."
  echo "  -n: the namespace used with the deployment."
  echo "  -r: restore a database from the file specified by the file option."
  echo "  -u: database user name."
  echo "  -h: display this help."
  echo
  echo "Examples:"
  echo "  Backup:"
  echo "    database-utilities.sh -n my-namespace -b -f ~/my-db-backup.dump"
  echo "  Restore:"
  echo "    database-utilities.sh -n my-namespace -r -f ~/my-db-backup.dump"
  echo
  echo
}

displayConfiguration() {
  echo "-------------------------------------"
  echo "Configured Options: "
  echo "  Mode:"
  echo "    backup:  $backup"
  echo "    restore: $restore"
  echo "  Backup/Restore File: $file"
  echo "  Deployment Namespace: $deploymentNamespace"
  echo "  Pod Search Keyword: $databaseKeyword"
  echo "  Database Name: $databaseName"
  echo "  Database User: $userName"
  echo "-------------------------------------"
}

checkContainerFound() {
  if [ -z $podId ];
    then
      echo
      echo "Database pod in names space $deploymentNamespace not found exiting ..."
      echo
      exit 1
    else
      echo
      echo "Database pod in namespace $deploymentNamespace found with ID: $podId"
      echo
  fi
}

checkFileSpecified() {
  if [ -z "$file" ];
    then
      echo "-f file option not specified."
      echo
      exit 1
  fi
}

backupDatabase() {
  checkFileSpecified "Cannot backup the database."
  echo "Backing up database $databaseName"
  kubectl -n $deploymentNamespace exec $podId -i -- pg_dump -Fc -U $userName -f /tmp/alert-database.dump $databaseName;
  kubectl -n $deploymentNamespace cp $podId:/tmp/alert-database.dump "$file"
  echo "Database $databaseName backup completed to file $file"
}

restoreDatabase() {
  checkFileSpecified "Cannot restore the database from a file."
  echo "Restoring database $databaseName from file $file"
  cat "$file" | kubectl -n $deploymentNamespace exec $podId -i -- pg_restore -U $userName -Fc --verbose --clean --if-exists -d $databaseName
  echo "Database $databaseName restored."
}

# script execution detail

if [ $# -eq 0 ];
  then
    usage
    exit 0
fi

while getopts "b,f:,d,k:,n:,r,u:,h" option; do
  case ${option} in
    b)
      backup=true
      ;;
    f)
      file="${OPTARG}"
      ;;
    d)
      databaseName="${OPTARG}"
      ;;
    k)
      databaseKeyword="${OPTARG}"
      ;;
    n)
      deploymentNamespace="${OPTARG}"
      ;;
    r)
      restore=true
      ;;
    u)
      userName="${OPTARG}"
      ;;
    h)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $option ${OPTARG}"
      usage
      ;;
  esac
done
shift $((OPTIND -1))

podId=$(kubectl -n $deploymentNamespace get pods | grep $databaseKeyword | awk '{print $1}');

displayConfiguration
checkContainerFound

# backup
if [ $backup == "true" ];
  then
    backupDatabase
fi

# restore
if [ $restore == "true" ];
  then
    restoreDatabase
fi

echo "$0 completed."