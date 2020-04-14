#!/bin/bash

DEFAULT_NAMESPACE="blackduck-alert"
NAMESPACE=$DEFAULT_NAMESPACE

if [ "$1" ]; then
  NAMESPACE=$1
fi

kubectl -n $NAMESPACE delete configmap blackduck-alert-config
kubectl -n $NAMESPACE delete deployment alert
kubectl -n $NAMESPACE delete service alert
kubectl -n $NAMESPACE delete deployment alertdb
kubectl -n $NAMESPACE delete service alertdb
kubectl -n $NAMESPACE delete deployment cfssl
kubectl -n $NAMESPACE delete service cfssl
kubectl delete namespace $NAMESPACE
