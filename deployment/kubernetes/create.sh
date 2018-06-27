#!/bin/bash

kubectl --namespace blackduck create configmap hub-alert-config --from-env-file=hub-alert.env
sleep 5
kubectl expose deployment alert --target-port=8443 --port 8443 -n blackduck --name=alert --type=LoadBalancer
sleep 5
kubectl create -f blackduck-alert.yml
