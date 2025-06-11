#!/bin/bash

usage() {
  echo "---------------------------------------------------------------------------------------"
  echo "- Usage: ./init-docker-db.sh                                                          -"
  echo "- Parameters                                                                          -"
  echo "-   -n: specify the container name to be created                                      -"
  echo "-   -p: postgres password                                                             -"
  echo "-   -e: exposed container port                                                        -"
  echo "-   -v: volume path                                                                   -"
  echo "-                                                                                     -"
  echo "- Example:                                                                            -"
  echo "    ./init-docker-db.sh -n alert-postgres -p blackduck -v ~/database/alert-10 -e 5432 -"
  echo "_______________________________________________________________________________________"
}

if [ $# -eq 0 ]
  then
      echo "---------------------------------------------------------------------------------------"
      echo "- Error: No arguments supplied                                                        -"
      echo "_______________________________________________________________________________________"
      usage 
      exit 1
fi
container_name='alert-postgres'
postgres_password='blackduck'
exposed_port=5432
volume_path=''

while getopts "e:,n:,p:,v:,h" option; do
  case ${option} in
    n) 
      container_name="${OPTARG}"
      ;;
    p)
      postgres_password="${OPTARG}"
      ;;
    e) 
      exposed_port="${OPTARG}"
      ;;
    v)
      volume_path="${OPTARG}"
      ;;
    h)
      usage
      exit 0
      ;;
    *) echo "Unknown option: $option ${OPTARG}"
       usage
       ;;
  esac
done
shift $((OPTIND -1))

echo "----------------------------"
echo "- Parameters               -"
echo "    Container Name:    $container_name"
echo "    Postgres Password: $postgres_password"
echo "    Exposed Port:      $exposed_port"
echo "    Local Volume Path: $volume_path"
echo "____________________________"

if [ -z $volume_path ]
  then
      echo "Volume Path not set."
      docker run -d --name "$container_name" -e POSTGRES_PASSWORD=$postgres_password --mount type=volume,source=$container_name-volume,destination=/var/lib/postgresql/data -p $exposed_port:5432 postgres:16.9-alpine
  else 
    docker run -d --name "$container_name" -e POSTGRES_PASSWORD=$postgres_password --mount type=bind,source=$volume_path,destination=/var/lib/postgresql/data -p $exposed_port:5432 postgres:16.9-alpine
fi

echo -n "Waiting for postgres "
while ! docker exec -i "$container_name" /usr/local/bin/psql -U postgres -h localhost -p 5432 postgres -c '\l' > /dev/null
do
  echo -n "."
  sleep 1
done
echo ""
echo ""
echo "Initializing database"
echo "---------------------"
docker exec -i "$container_name" /usr/local/bin/psql -U postgres -h localhost -p 5432 postgres -c "CREATE ROLE sa WITH LOGIN SUPERUSER PASSWORD 'blackduck';"
docker exec -i "$container_name" /usr/local/bin/psql -U postgres -h localhost -p 5432 postgres -c "CREATE DATABASE alertdb WITH OWNER sa;"
docker exec -i "$container_name" /usr/local/bin/psql -U sa -h localhost -p 5432 alertdb -c "CREATE EXTENSION \"uuid-ossp\""
docker exec -i "$container_name" /usr/local/bin/psql -U postgres -h localhost -p 5432 alertdb -c "alter role sa set search_path = public,alert"
echo "---------------------"
echo "Created database container: $container_name"
