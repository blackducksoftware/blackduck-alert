# Black Duck Alert On Kubernetes.

## Requirements

### Installing Alert

All below commands assume:
- you are using the namespace (or openshift project name) 'blackduck-alert'.
- you have a cluster with at least 1GB of allocatable memory.
- you have administrative access to your cluster.

### Step 1: Create the configuration map for Alert

Create the config map from the env file to specify the environment variables for alert

```
kubectl -n blackduck-alert create configmap blackduck-alert-config --from-env-file=blackduck-alert.env
```

#### Step 2: Create your cfssl container, and the Alert config map.

Setup the cfssl container for managing certificates

```
kubectl create -f 1-cfssl.yml -n blackduck-alert
```

#### Step 3: Create the Alert app's containers.
Now setup the alert containers for the application

```
kubectl create -f 2-alert.yml -n blackduck-alert
```


#### Using a Custom web server certificate-key pair

Alert allows users to use their own web server certificate-key pairs for establishing ssl connection.

* Create a Kubernetes secret each called 'WEBSERVER_CUSTOM_CERT_FILE' and 'WEBSERVER_CUSTOM_KEY_FILE' with the custom certificate and custom key in your namespace.

You can do so by

```
kubectl secret create WEBSERVER_CUSTOM_CERT_FILE --from-file=<certificate file>
kubectl secret create WEBSERVER_CUSTOM_KEY_FILE --from-file=<key file>
```

For the webserver service, add secrets by copying their values into 'env'
values for the pod specifications in the webserver.
