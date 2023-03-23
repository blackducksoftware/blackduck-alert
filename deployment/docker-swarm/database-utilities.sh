#!/bin/bash
set -e

# defaults
backup=false
restore=false
plainFormat=false
file=
databaseKeyword=alertdb
databaseName=alertdb
userName=sa
type="plain"

# functions
usage() {
  echo "usage: database-utilities - backup or restore a database with docker."
  echo
  echo "database-utilities.sh [-b] [-d databaseName] [-f file] [-k containerKeyword] [-p] [-r] [-t type] [-u userName]"
  echo "Options: "
  echo "  -b: backup a database to the file specified in the file option."
  echo "  -d: the name of the database."
  echo "  -f: the file to save a backup or the file to restore the database from."
  echo "  -k: the keyword to search for the database container."
  echo "  -p: plain text database dump format"
  echo "  -r: restore a database from the file specified by the file option."
  echo "  -t: the format for the backup or restore file of 'plain' or 'binary'."
  echo "  -u: database user name."
  echo "  -h: display this help."
  echo
  echo "Examples:"
  echo " ------------------"
  echo " Plain Text Format:"
  echo " ------------------"
  echo "  Backup (default):"
  echo "    database-utilities.sh -b -f ~/my-db-backup.dump"
  echo "  Restore (default):"
  echo "    database-utilities.sh -r -f ~/my-db-backup.dump"
  echo "  Backup:"
  echo "    database-utilities.sh -b -t plain -f ~/my-db-backup.dump"
  echo "  Restore:"
  echo "    database-utilities.sh -r -t plain -f ~/my-db-backup.dump"
  echo " ------------------"
  echo " Binary Format:"
  echo " ------------------"
  echo "  Backup:"
  echo "    database-utilities.sh -b -t binary -f ~/my-db-backup.dump"
  echo "  Restore:"
  echo "    database-utilities.sh -r -t binary -f ~/my-db-backup.dump"
  echo
  echo
}

displayConfiguration() {
  echo "-------------------------------------"
  echo "Configured Options: "
  echo "  Mode:"
  echo "    backup:                 $backup"
  echo "    restore:                $restore"
  echo "  Format:                   $type"
  echo "  Backup/Restore File:      $file"
  echo "  Container Search Keyword: $databaseKeyword"
  echo "  Database Name:            $databaseName"
  echo "  Database User:            $userName"
  echo "-------------------------------------"
}

checkFormat() {
  if [ "$type" == "PLAIN" ];
    then
      plainFormat=true
  elif [ "$type" == "plain" ];
    then
      plainFormat=true
  elif [ "$type" == "BINARY" ];
    then
      plainFormat=false
  elif [ "$type" == "binary" ];
    then
      plainFormat=false
  else
      echo "Unknown format: Only 'plain' or 'binary' are allowed."
      exit 1
  fi
}

checkContainerFound() {
  if [ -z $containerId ];
    then
      echo
      echo "Database container not found exiting ..."
      echo
      exit 1
    else
      echo
      echo "Database container found with ID: $containerId"
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
  if [ $plainFormat == "true" ];
    then
      docker exec -i $containerId pg_dump -Fp -U $userName --clean -f /tmp/alert-database.dump $databaseName;
    else
      docker exec -i $containerId pg_dump -Fc -U $userName -f /tmp/alert-database.dump $databaseName;
  fi
  docker cp $containerId:/tmp/alert-database.dump "$file"
  echo "Database $databaseName backup completed to file $file"
}

restoreDatabase() {
  checkFileSpecified "Cannot restore the database from a file."
  echo "Restoring database $databaseName from file $file"
  if [ $plainFormat == "true" ];
    then
      cat "$file" | docker exec -i $containerId psql -U $userName $databaseName
    else
      cat "$file" | docker exec -i $containerId pg_restore -U $userName -Fc --verbose --clean --if-exists -d $databaseName
  fi
  echo "Database $databaseName restored."
}

# script execution detail

if [ $# -eq 0 ];
  then
    usage
    exit 0
fi

while getopts "b,d:,f:,k:,p,r,t:,u:,h" option; do
  case ${option} in
    b)
      backup=true
      ;;
    d)
      databaseName="${OPTARG}"
      ;;
    f)
      file="${OPTARG}"
      ;;
    k)
      databaseKeyword="${OPTARG}"
      ;;
    p)
      plainFormat=true
      ;;
    r)
      restore=true
      ;;
    t)
      type="${OPTARG}"
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

containerId=$(docker ps -q --filter name=$databaseKeyword);

displayConfiguration
checkFormat
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