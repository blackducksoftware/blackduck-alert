#!/bin/bash

usage() {
  echo "--------------------------------------------------------------"
  echo "- Usage: ./init-helm-db.sh                                   -"
  echo "- Parameters                                                 -"
  echo "-   -n: specify the path to the helm chart                   -"
  echo "-   -n: specify the namespace name to be created             -"
  echo "-   -i: postgres password                                    -"
  echo "-                                                            -"
  echo "- Example:                                                   -"
  echo "    ./init-helm-db.sh -n postgres-namespace -i install-name  -"
  echo "_____________________________________________________________-"
}

if [ $# -eq 0 ];
  then
      echo "--------------------------------------------------------------"
      echo "- Error: No arguments supplied                               -"
      echo "______________________________________________________________"
      usage
      exit 1
fi

namespace='test-postgres'
installation_name='alert-install'
chart_path="."
user=sa
db_name=alertdb

while getopts "c:,d:,n:,i:,u:,h" option; do
  case ${option} in
    c)
      chart_path="${OPTARG}"
      ;;
    d)
      db_name="${OPTARG}"
      ;;
    n)
      namespace="${OPTARG}"
      ;;
    i)
      installation_name="${OPTARG}"
      ;;
    u)
      user="${OPTARG}"
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

echo "----------------------------"
echo "- Parameters               -"
echo "    Namespace:         $namespace"
echo "    Installation Name: $installation_name"
echo "    Helm Chart Path:   $chart_path"
echo "    Database User:     $user"
echo "    Database Name:     $db_name"
echo "____________________________"

kubectl create namespace $namespace
helm -n $namespace install $installation_name $chart_path

while ! kubectl exec -n $namespace -i $(kubectl -n $namespace get pods | grep $installation_name | awk '{print $1}') -- psql -U $user -h localhost -p 5432 $db_name -c '\l' > /dev/null
do
  echo -n "."
  sleep 1
done
echo
echo "Database connection established."
echo "Initializing database."
kubectl exec -n $namespace -i $(kubectl -n $namespace get pods | grep $installation_name | awk '{print $1}') -- psql -U $user -h localhost -p 5432 $db_name -c "CREATE EXTENSION \"uuid-ossp\""
echo "Initialization complete."