#!/bin/bash
#defaults
backup=false
restore=false
file=
databaseKeyword=alertdb
dumpSchema=false
databaseName=alertdb
userName=sa
password=

displayConfiguration() {
  if [ -z $password ];
    then
      databasePasswordSet=false
    else
      databasePasswordSet=true
  fi
  echo "-------------------------------------"
  echo "Configured Options: "
  echo "  Mode:"
  echo "    backup:  $backup"
  echo "    restore: $restore"
  echo "  Input/Output File:"
  echo "    File: $file"
  echo "  Container Search Keyword:"
  echo "    Keyword: $databaseKeyword"
  echo "  Dump Database Schema: $dumpSchema"
  echo "  Database Name: $databaseName"
  echo "  Database User: $userName"
  echo "  Database Password Set: $databasePasswordSet"
  echo "-------------------------------------"
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
  if [-z $file ];
    then
      echo "-f file option not specified. $1"
      echo
      exit 1
  fi
}

while getopts "b,f:,k:,r,s,u:,p:" option; do
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
    r)
      restore=true
      ;;
    s)
      dumpSchema=true
      ;;
    u)          ``
      userName="${OPTARG}"
      ;;
    p)
      password="${OPTARG}"
      ;;
    *)
      echo "Unknown option: $option ${OPTARG}"
      ;;
  esac
done
shift $((OPTIND -1))

containerId=$(docker ps | grep $databaseKeyword | awk '{print $1}');

displayConfiguration
checkContainerFound

if [ $backup == "true" ];
  then
    checkFileSpecified "Cannot backup the database."
    echo "Backing up database $databaseName"
    docker exec -i $containerId pg_dump -Fc -U $userName -f /tmp/alert-database.dump $databaseName;
    docker cp $containerId:/tmp/alert-database.dump $file
    echo "Database $databaseName backup completed to file $file"
fi

if [ $restore == "true" ];
  then
    checkFileSpecified "Cannot restore the database from a file."
    echo "Restoring database $databaseName from file $file"
    cat $file | docker exec -i $containerId pg_restore -U $userName -Fc --verbose --clean --if-exists -d $databaseName
    echo "Database $databaseName restored."
fi

# restore the database