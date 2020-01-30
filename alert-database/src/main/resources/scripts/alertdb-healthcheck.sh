#!/usr/bin/env bash
if psql "host=alertdb port=5432 dbname=postgres user=sa password=blackduck" -c '\l' |grep -q 'alertdb';
then
    exit 0
else
    exit 1
fi
