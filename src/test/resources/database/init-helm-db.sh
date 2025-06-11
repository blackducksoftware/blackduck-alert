#!/bin/bash
set -e

formatLine() {
  local text="$1"
  local prefix="- "
  local suffix="-"
  local total_length=62

  # Special case: if the input is "-----", print all dashes
  if [[ "$text" == "-----" ]]; then
    printf '%*s\n' $total_length '' | tr ' ' '-'
    return
  fi

  local padding_length=$((total_length - ${#prefix} - ${#text} - 1))
  local padding=$(printf '%*s' "$padding_length")

  echo "${prefix}${text}${padding}${suffix}"
}

usage() {
  formatLine "-----"
  formatLine "Usage: ./init-helm-db.sh"
  formatLine "Parameters"
  formatLine "  -c: specify the path to the Helm chart"
  formatLine "  -d: specify the name of the Alert database"
  formatLine "  -n: specify the Kubernetes namespace name to be created"
  formatLine "  -i: specify the Helm install name"
  formatLine "  -u: specify the Alert DB user name"
  formatLine ""
  formatLine "Example:"
  formatLine "  ./init-helm-db.sh -n postgres-namespace -i install-name"
  formatLine ""
  formatLine "Defaults:"
  formatLine "   -c: ${chart_path}"
  formatLine "   -d: ${db_name}"
  formatLine "   -n: ${namespace}"
  formatLine "   -i: ${installation_name}"
  formatLine "   -u: ${user}"
  formatLine "-----"
}

chart_path="."
db_name='alertdb'
namespace='test-postgres'
installation_name='alert-install'
user='sa'

if [ $# -eq 0 ];
  then
      formatLine "-----"
      formatLine "Error: No arguments supplied"
      formatLine "-----"
      usage
      exit 1
fi

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