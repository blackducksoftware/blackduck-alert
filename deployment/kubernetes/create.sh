#!/bin/bash

kubectl create namespace blackduck-alert
sleep 5
kubectl -n blackduck-alert create configmap blackduck-alert-config --from-env-file=blackduck-alert.env
sleep 5
kubectl create -f 2-cfssl.yml -n blackduck-alert
sleep 5
kubectl create -f 3-alert.yml -n blackduck-alert
sleep 5
#kubectl expose deployment alert --target-port=8443 --port 8443 -n blackduck-alert --name=alert --type=LoadBalancer
