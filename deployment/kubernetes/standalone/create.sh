#!/bin/bash

DEFAULT_NAMESPACE="blackduck-alert"
NAMESPACE=$DEFAULT_NAMESPACE

if [ "$1" ]; then
  NAMESPACE=$1
fi

kubectl create namespace $NAMESPACE
sleep 5
kubectl create -f 1-cm-alert.yml -n $NAMESPACE
sleep 5
kubectl create -f 2-cfssl.yml -n $NAMESPACE
sleep 5
kubectl create -f 3-alert.yml -n $NAMESPACE
sleep 5
