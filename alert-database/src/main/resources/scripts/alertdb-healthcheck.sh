#!/usr/bin/env bash
if psql "host=alertdb port=5432 dbname=postgres user=${POSTGRES_USER} password=${POSTGRES_PASSWORD}" -c '\l' |grep -q 'alertdb';
then
    exit 0
else
    exit 1
fi
