#!/bin/bash

DEFAULT_NAMESPACE="blackduck-alert"
NAMESPACE=$DEFAULT_NAMESPACE

if [ "$1" ]; then
  NAMESPACE=$1
fi

kubectl create namespace $NAMESPACE
sleep 5
kubectl apply -f 1-cm-alert.yml -n $NAMESPACE
sleep 5
kubectl apply -f 2-cfssl.yml -n $NAMESPACE
sleep 5
kubectl apply -f 3-alertdb.yml -n $NAMESPACE
sleep 5
kubectl apply -f 4-alert.yml -n $NAMESPACE
sleep 5
