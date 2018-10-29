# Black Duck Alert On Kubernetes.

## Requirements

The 3-alert.yml file contains a volume specification.  Currently it is configured as an emptyDir, which is not persistent storage.
Before installing or upgrading Alert edit this file to specify the desired persistent storage. 

### Upgrading Alert
The steps in the upgrade procedure are the same as the installation procedure.  
The difference is that the kubectl apply command is used in place of the kubectl create command.

```
kubectl create
```
changes to:
```
kubectl apply
``` 

Please review the installation instructions below.

### Installing Alert

### Standalone Installation
All below commands assume:
- you are using the namespace (or openshift project name) 'blackduck-alert'.
- you have a cluster with at least 1GB of allocatable memory.
- you have administrative access to your cluster.

#### Step 1: Create the configuration map for Alert

Create the config map from the file to specify the environment variables for Alert
Edit the 1-cm-alert.yml file with the environment variable values you want to use for your configuration of Alert.
###### Note:  You can edit the contents of the configuration map after it has been created. ######

```
kubectl create -f 1-cm-alert.yml -n blackduck-alert
```

#### Step 2: Create your cfssl container, and the Alert config map

Setup the cfssl container for managing certificates

```
kubectl create -f 2-cfssl.yml -n blackduck-alert
```

#### Step 3: Create the Alert application's containers
Now setup the Alert containers for the application

```
kubectl create -f 3-alert.yml -n blackduck-alert
```

##### Quick start
 For your convenience 
 * Create Alert deployment:
    * Execute the bundled create.sh file.  It will create a blackduck-alert namespace and deploy the application.
    * Different namespace: create.sh <your_alert_namespace>
 * Delete Alert Deployment:
    * Execute the bundled delete.sh file.  It will delete the deployment in the blackduck-alert namespace.
    * Different namespace: delete.sh <your_alert_namespace>

### Installation with the BlackDuck

You can install the Alert container as part of your BlackDuck installation.  This section describes the steps to install Alert with BlackDuck.

#### Step 1: Install the BlackDuck
 * During installation update the hub-config Configuration Map to have the variable USE_ALERT: "1"

#### Step 2: Create the configuration map for Alert
Edit the 1-cm-alert.yml file with the environment variable values you want to use for your configuration of Alert.
###### Note:  You can edit the contents of the configuration map after it has been created.

```
kubectl create -f 2-cm-alert.yml -n <your_blackduck_namespace>
```

#### Step 3: Create the Alert application's container

```
kubectl create -f 3-alert.yml -n <your_blackduck_namespace>
```

## Using a Custom web server certificate-key pair

Alert allows users to use their own web server certificate-key pairs for establishing ssl connection.

* Create a Kubernetes secret each called 'WEBSERVER_CUSTOM_CERT_FILE' and 'WEBSERVER_CUSTOM_KEY_FILE' with the custom certificate and custom key in your namespace.

You can do so by

```
kubectl secret create WEBSERVER_CUSTOM_CERT_FILE --from-file=<certificate file>
kubectl secret create WEBSERVER_CUSTOM_KEY_FILE --from-file=<key file>
```

For the webserver service, add secrets by copying their values into 'env'
values for the pod specifications in the webserver.
