#!/bin/bash

kubectl -n blackduck delete configmap blackduck-alert-config
kubectl -n blackduck delete deployment alert
kubectl -n blackduck delete service alert
