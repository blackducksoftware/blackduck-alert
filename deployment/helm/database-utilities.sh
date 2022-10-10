#!/bin/bash
#defaults
backup=false
restore=false
file=
databaseKeyword="myalert-postgres-"
dumpSchema=false
databaseName=alertdb
userName=sa
password=
deploymentNamespace=default

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
  echo "  Deployment Namespace: $deploymentNamespace"
  echo "  Pod Search Keyword:"
  echo "    Keyword: $databaseKeyword"
  echo "  Dump Database Schema: $dumpSchema"
  echo "  Database Name: $databaseName"
  echo "  Database User: $userName"
  echo "  Database Password Set: $databasePasswordSet"
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
  if [-z $file ];
    then
      echo "-f file option not specified. $1"
      echo
      exit 1
  fi
}

while getopts "b,f:,k:,n:,r,s,u:,p:" option; do
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

podId=$(kubectl -n $deploymentNamespace get pods | grep $databaseKeyword | awk '{print $1}');

displayConfiguration
checkContainerFound

# backup
if [ $backup == "true" ];
  then
    checkFileSpecified "Cannot backup the database."
    echo "Backing up database $databaseName"
    kubectl -n $deploymentNamespace exec $podId -i -- pg_dump -Fc -U $userName -f /tmp/alert-database.dump $databaseName;
    kubectl -n $deploymentNamespace cp $podId:/tmp/alert-database.dump $file
    echo "Database $databaseName backup completed to file $file"
fi

# restore
if [ $restore == "true" ];
  then
    checkFileSpecified "Cannot restore the database from a file."
    echo "Restoring database $databaseName from file $file"
    cat $file | kubectl -n $deploymentNamespace exec $podId -i -- pg_restore -U $userName -Fc --verbose --clean --if-exists -d $databaseName
    echo "Database $databaseName restored."
fi