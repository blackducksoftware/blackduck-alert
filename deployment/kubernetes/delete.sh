#!/bin/bash

kubectl -n sb-hub-stuff delete configmap hub-alert-config
kubectl -n sb-hub-stuff delete deployment alert
kubectl -n sb-hub-stuff delete service alert
