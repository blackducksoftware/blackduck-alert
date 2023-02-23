#!/bin/bash
kubectl -n $1 get pods | grep $2 | awk '{print $1}'
