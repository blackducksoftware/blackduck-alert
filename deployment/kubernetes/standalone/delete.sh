#!/bin/bash

kubectl -n blackduck-alert delete configmap blackduck-alert-config
kubectl -n blackduck-alert delete deployment alert
kubectl -n blackduck-alert delete service alert
kubectl -n blackduck-alert delete deployment cfssl
kubectl -n blackduck-alert delete service cfssl
kubectl delete namespace blackduck-alert
