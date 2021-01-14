# alert-helm
Helm Charts for Synopsys Alert

## Table Of Contents
- [Prerequisites](#prerequisites)
- [Installing the Chart -- Helm 2](#installing-the-chart----helm-2)
- [Installing the Chart -- Helm 3](#installing-the-chart----helm-3)
    - [Quick Start with Helm 3](#quick-start-with-helm-3)
- [Finding Alert External Port](#finding-alert-external-port)
- [Upgrading the Chart](#upgrading-the-chart)
- [Uninstalling the Chart](#uninstalling-the-chart)
- [Configuration](#configuration)
  - [Configuration Parameters](#configuration-parameters)
  - [Custom Certificates](#custom-certificates)
  - [Persistent Storage](#persistent-storage)
  - [External Postgres Database](#external-postgres-database)
  - [Database Admin User Password](#database-admin-user-password)
  - [Installing with Black Duck](#installing-with-black-duck)

## Prerequisites

- Kubernetes 1.9+
- Helm2 or Helm3

## Installing the Chart -- Helm 2

### Create the Namespace
```bash
$ kubectl create ns <namespace>
```
### Configure your Alert Instance
Modify the values.yaml file or pass in values to `helm install` with --set.  

### Install the Alert Chart
```bash
$ helm install . --name <name> --namespace <namespace>
```

> **Tip**: List all releases using `helm list`


## Installing the Chart -- Helm 3

### Create the Namespace and TLS Secrets
```bash
$ kubectl create ns <namespace>
```
### Configure your Alert Instance
Modify the values.yaml file or pass in values to `helm install` with --set.  
Please see the [Configuration](#configuration) section for more details.

### Install the Alert Chart
```bash
$ helm install <name> . --namespace <namespace>
```

### Quick Start with Helm 3
#### Step 1
Navigate to the alert-helm chart repository in your terminal
```bash
$ cd <path>/alert-helm
```

#### Step 2
```bash
$ kubectl create ns myalert
```

#### Step 3
Deploy Alert
```bash
$ helm install myalert . --namespace myalert
```

## Finding Alert External Port
 Once Alert has been deployed if the `exposeui` parameter is true, then Alert will be available via an exposed port.
 To determine the port to access the Alert UI execute the following command:
 ```bash 
 $ kubectl -n <NAMESPACE> get services
 ```
 From the output find the Alert exposed service. This service will be your Alert installation name with the `-exposed` suffix in the name. 
 If the installation name is 'myalert' then there will be a service `myalert-exposed` in the list of services.
 
 It will display a port with the following format:
  
  `<INTERNAL_PORT>:<EXTERNAL_PORT>/TCP`
  
  For example:
  
  `8443:31594/TCP`
  
  The internal port is 8443 and the external port is 31594. When accessing the Alert UI the external port will be used in the URL.
  Once the external port is identified the URL to access the UI will be in the following format:
  
  `https://<EXTERNAL_NODE_IP>:<EXTERNAL_PORT>/alert`
  
  ```bash
  $ kubectl get nodes -o wide
  ```
  
  For Example: 
  
  `https://127.0.0.0:31594/alert`

## Upgrading the Chart

```bash
$ helm upgrade <name> . --namespace <namespace>
```

## Uninstalling the Chart

To uninstall/delete the deployment:

```bash
$ helm delete <name> 
```

The command removes all the Kubernetes components associated with the chart and deletes the release.

## Configuration

The following table lists the configurable parameters of the Alert chart and their default values.

### Configuration Parameters
This contains a table briefly describing each parameter in the values.yaml file.

| Parameter | Description | Default |
| --------- | ----------- | ------- |
| `alert.imageTag` | Image tag for the Alert container | `docker.io/blackducksoftware/blackduck-alert:ALERT_VERSION_TOKEN` |
| `alert.registy` | The container registry for the Alert pod | `""` |
| `alert.port` | The internal port the Alert pod will use | `8443` |
| `alert.resources.limits.memory` | Alert container Memory Limit | `2560M` |
| `alert.resources.requests.memory` | Alert container Memory Request | `2560M` |
| `alert.persistentVolumeClaimName` | The name of the persistent storage claim | `""` |
| `alert.claimSize` | The persistent storage claim size limit | `5Gi` |
| `alert.storageClass` | The name of the storage class for persistent storage | `""` |
| `alert.volumeName` | The name of the persistent storage volume | `""` |
| `alert.nodeSelector` | Alert node labels for pod assignment | `{}` | 
| `alert.tolerations` | Alert node tolerations for pod assignment | `[]` |
| `alert.affinity` | Alert node affinity for pod assignment | `{}` |
| `alert.securityContext` | Alert security context | `{}` |
| `alert.podSecurityContext` | Alert pod security context | `{}` |
| `cfssl.imageTag` | Image for the Cfssl container | `docker.io/blackducksoftware/blackduck-cfssl:1.0.1` |
| `cffsl.registy` | The container registry for the Cfssl pod | `""` |
| `cfssl.resources.limits.memory` | Cfssl container Memory Limit | `640M` |
| `cfssl.resources.requests.memory` | Cfssl container Request Limit | `640M` |
| `cfssl.nodeSelector` | Cfssl node labels for pod assigment | `{}` |
| `cfssl.tolerations` | Cfssl node tolerations for pod assignment | `[]` |
| `cfssl.affinity` | Cfssl node affinity for pod assignment | `{}` |
| `cfssl.securityContext` | Cfssl node security context | `{}` |
| `cfssl.podSecurityContext` | Cfssl pod security context | `{}` |
| `postgres.registry` |  Postgres registry containing image for the container | `"docker.io/centos"` |
| `postgres.isExternal` |  If true, do not deploy a Postgres container |  `false` |
| `postgres.host` |  Host name of the Postgres database | `""` |
| `postgres.port` |  Port of the Postgres database | `5432` |
| `postgres.userUserName` | Postgres database user owning the database Alert uses | `sa` |
| `postgres.userPassword` | Postgres database password for the user | `blackduck` |
| `postgres.databaseName` | Postgres database name where Alert data will be stored | `alertdb` |
| `postgres.adminUserName` | Postgres database admin user | `postgres` |
| `postgres.adminPassword` | Postgres database password for the admin user | `""` |
| `postgres.persistentVolumeClaimName` | Postgres node volume claim name | `""` | 
| `postgres.claimSize` | Postgres node volume claim size | `"5Gi"` |
| `postgres.storageClass` | Postgres node storage class for volume claim | `""` |
| `postgres.volumeName` | Postgres node volume name for pod assignment | `""` |
| `postgres.nodeSelector` | Postgres node labels for pod assignment | `{}` |
| `postgres.tolerations` | Postgres node tolerations for pod assignment | `[]` |
| `postgres.affinity` | Postgres node affinity for pod assignment | `{}` |
| `postgres.podSecurityContext` | Postgres node pod security context | `{}` |
| `postgres.securityContext` | Postgres node security context | `{}` |
| `postgres.resources` | Postrges node resources | `{}` |
| `blackDuckName` | The ReleaseName of the Black Duck instance | `""` |
| `blackDuckNamespace` | The Namespace of the Black Duck instance | `""` |
| `deployAlertWithBlackDuck` | If true, Alert will be configured to run with a Black Duck instance | `false` |
| `enableCertificateSecret` | If true, Alert will use values in a Secret for authenticating it's certificates | `false` |
| `enableStandalone` | If true, Alert will be deployed with it's own cfssl instance | `true` |
| `enablePersistentStorage` | If true, Alert will have persistent storage | `true` |
| `environs` | Environment variables for the Alert container | `[]` |
| `exposeui` | If true, a Service to expose the UI will be created | `true` |
| `exposedServiceType` | Type of exposed Service | `NodePort` |
| `exposedNodePort` | The exposed NodePort | `` |
| `imagePullSecrets` | Pull secrets to download images | `[]` |
| `javaKeystoreSecretName` | (deprecated) The name of the secret for the Java certificate truststore | `""` |
| `pvcSize` | Persistent Volume Claim claim size | `5G` |
| `secretEnvirons` | Sensitive environment variables for the Alert container to be stored in a Secret | `[]` |
| `setEncryptionSecretData` | If true, you will be prompted to set values for encrypting Alert's data | `false` |
| `status` | Used to start or stop alert instance | `Running` |
| `storageClassName` | Persistent Volume Claim storage class | `""` |
| `webserverCustomCertificatesSecretName` | The name of the secret containing the SSL certificate and private key for Alert server | `""` |

Specify each parameter using the `--set key=value[,key=value]` argument to `helm install`.

Alternatively, a YAML file that specifies the values for the above parameters can be provided while installing the chart. For example,

```bash
$ helm install . --name <name> --namespace <namespace> --set enableStandalone=true
```

### Custom Certificates
This section describes how to configure the Alert webserver with a custom certificate.

#### Create Certificate Secret
- Execute the command
  ```bash 
   $ kubectl create secret generic <SECRET_NAME> -n <ALERT_NAMESPACE> \
   --from-file=WEBSERVER_CUSTOM_CERT_FILE=<PATH_TO_CERTIFICATE_FILE> \
   --from-file=WEBSERVER_CUSTOM_KEY_FILE=<PATH_TO_CERTIFICATE_KEY_FILE>
  ```
  - Replace `<SECRET_NAME>` with the desired name for the secret.
  - Replace `<ALERT_NAMESPACE>` with the namespace being used for Alert.
  - Replace `<PATH_TO_CERTIFICATE_FILE>` to the path on the current file system to your `.crt` file.
  - Replace `<PATH_TO_CERTIFICATE_KEY_FILE>` to the path on the current file system to the `.key` file corresponding to your `.crt` file.
  - Note: The keys `WEBSERVER_CUSTOM_CERT_FILE` and `WEBSERVER_CUSTOM_KEY_FILE` must be included 
    in the `--from-file=[key=]<FILE_NAME>` arguments in order for Alert to correctly consume the certificate.

For more information about managing secrets, please see: https://kubernetes.io/docs/tasks/configmap-secret/managing-secret-using-kubectl

#### Configure Certificate Secret
Once you have created the certificate secret with the correct keys, you must then tell alert the name of the 
secret the certificate corresponds to.
- In the 'values.yaml' file, set
  ```yaml
  webserverCustomCertificatesSecretName: "<SECRET_NAME>"
  ```
  - Replace `<SECRET_NAME>` with the name of the secret created in the step [Create Certificate Secret](#create-certificate-secret).
  - Note: This will not automatically enable the use of the custom certificate. 
    To do that, follow the instructions in [Enable Custom Certificate](#enable-custom-certificate).

#### Enable Custom Certificate
- In the 'values.yaml' file, set
  ```yaml
  enableCertificateSecret: true
  ```
  - This will instruct Alert to use the secret specified by the `webserverCustomCertificatesSecretName` 
    configuration parameter as the custom certificate.

### Persistent Storage
The section will describe the changes needed to configure persistent storage.  

Note: If you are using the HostPath persistent volume then you must ensure the correct permissions and ownership.
If you are using the security context then the ownership must match the UID of the user and the GID of the group specified
in the security context.  The file permissions must allow a write to the HostPath volume.
Please see: https://unofficial-kubernetes.readthedocs.io/en/latest/concepts/storage/volumes/#hostpath for further details.

#### Enable Persistent Storage
- In the 'values.yaml' file ensure 
  ```yaml
  enablePersistentStorage: true 
  ```
  - This is the default value to prevent loss of data  
  - Alert will not startup correctly if this is set to 'true' and persistent volumes are note configured
  - If this is false when the deployment is uninstalled all data will be lost
  
#### With Storage Claims
This section defines configuration using Persistent Volume Claims.  Claims can be optionally used rather than just a Persistent volume.
You must have a claim created for the 'alert' service regardless of an on-premise or external database.
For the on-premise database deployment a second Persistent Volume Claim must be created to store the database data.

##### Alert Volume Claim Configuration
- A persistent volume must be created first if a dynamic provisioner not being used
- A persistent volume claim must be created in the same namespace as the Alert deployment and bound to the persistent volume for Alert
- Configure Alert to use the volume claim

  ```yaml
  alert:
    persistentVolumeClaimName: "<ALERT_CLAIM_NAME>"
  ```
  
  - Replace <ALERT_CLAIM_NAME> with the name of the persistent volume claim for Alert data
  - An optional storage class with the persistent volume claim requires the addition of the storage class name
  
    ```yaml
    alert:
      persistentVolumeClaimName: "<ALERT_CLAIM_NAME>"
      storageClassName: "<STORAGE_CLASS_NAME>"  
    ```
    
    - Replace <STORAGE_CLASS_NAME> with the name of the storage class in the persistent volume claim

  - Example:
  ```yaml
  alert:
    persistentVolumeClaimName: "alert-pvc"
    storageClassName: "myStorageClass"
  ```
    
##### Using On-Premise Database
- A persistent volume must be created first for the Postgres database if a dynamic provisioner not being used
- A persistent volume claim must be created in the same namespace as the Alert deployment for Postgres and bound to the persistent volume for Postgres
- Configure Postgres to use the volume claim

  ```yaml
  postgres:
    persistentVolumeClaimName: "<POSTGRES_CLAIM_NAME>"
  ```
  
  - Replace <POSTGRES_CLAIM_NAME> with the name of the persistent volume claim for Postgres data
  - An optional storage class with the persistent volume claim requires the addition of the storage class name
  
    ```yaml
    postgres:
      persistentVolumeClaimName: "<POSTGRES_CLAIM_NAME>"
      storageClassName: "<STORAGE_CLASS_NAME>"  
    ```
    
    - Replace <STORAGE_CLASS_NAME> with the name of the storage class in the persistent volume claim
  - Example:
    ```yaml
    postgres:
      persistentVolumeClaimName: "postgres-pvc"
      storageClassName: "myStorageClass"
    ```

#### Without Storage Claims
This section defines configuration using Persistent Volume.  Claims will automatically be created and bound to the volumes defined.
You must have a Persistent Volume created for the 'alert' service regardless of an on-premise or external database.
For the on-premise database deployment a second Persistent Volume must be created to store the database data.

##### Alert Volume Configuration
- A persistent volume must be created first if a dynamic provisioner not being used
- Configure Alert to use the volume name

  ```yaml
  alert:
    volumeName: "<ALERT_VOLUME_NAME>"
  ```
  
  - Replace <ALERT_VOLUME_NAME> with the name of the persistent volume for Alert data
  - An optional storage class with the persistent volume requires the addition of the storage class name
  
    ```yaml
    alert:
      volumeName: "<ALERT_VOLUME_NAME>"
      storageClassName: "<STORAGE_CLASS_NAME>"  
    ```
    
    - Replace <STORAGE_CLASS_NAME> with the name of the storage class in the persistent volume claim
  - Define the claim size by default it is 5GB
    
    ```yaml
    alert:
      claimSize: "5Gi"
    ```
  - Example:
  ```yaml
  alert:
    claimSize: "5Gi"
    storageClassName: "myStorageClass"
    volumeName: "alert-volume"
  ```  
  - A claim will be created with the release name for example 'myalert-pvc' please verify the claim bound to the volume
  
  ```bash
  $ kubectl -n <ALERT_NAMESPACE> get pvc
  ```
  
##### Using On-Premise Database
- A persistent volume must be created first for the Postgres database if a dynamic provisioner not being used
- Configure Alert to use the volume name

  ```yaml
  postgres:
    volumeName: "<POSTGRES_VOLUME_NAME>"
  ```
  
  - Replace <POSTGRES_VOLUME_NAME> with the name of the persistent volume for Postgres data
  - An optional storage class with the persistent volume requires the addition of the storage class name
  
    ```yaml
    postgres:
      volumeName: "<POSTGRES_VOLUME_NAME>"
      storageClassName: "<STORAGE_CLASS_NAME>"  
    ```
    
    - Replace <STORAGE_CLASS_NAME> with the name of the storage class in the persistent volume claim
  - Define the claim size by default it is 5GB
    
    ```yaml
    postgres:
      claimSize: "5Gi"
    ```
  - Example:
    ```yaml
    postgres:
      claimSize: "5Gi"
      storageClassName: "myStorageClass"
      volumeName: "postgres-volume"
    ```  
    - A claim will be created with the release name for example 'myalert-postgres' please verify the claim bound to the volume
    
    ```bash
    $ kubectl -n <ALERT_NAMESPACE> get pvc
    ```
    
### External Postgres Database
- On the external database create a user to own the Alert database

  ```sql 
  CREATE ROLE <ROLE_NAME> LOGIN PASSWORD '<PASSWORD>'
  ```
  - Replace <ROLE_NAME> with the user name for the database
  - Replace <PASSWORD> with the password for the database user
  
- On the external database create a database owned by the user just created

  ```sql
  CREATE DATABASE <DATABASE_NAME> WITH OWNER <ROLE_NAME>
  ```
  - Replace <DATABASE_NAME> with the name of the database for Alert
  - Replace <ROLE_NAME> with the Alert database user created in the previous step
  
- Disable on premise database container creation

  ```yaml
  postgres:
    isExternal: true
  ```
  
- Configure alert Postgres user by setting 'userUserName'

  ```yaml
  postgres:
    userUserName: <ROLE_NAME>
  ```
  - Replace <ROLE_NAME> with the database user name
  
- Configure alert Postgres password by setting 'userPassword'

  ```yaml 
  postgres:
    userPassword: <PASSWORD>
  ```
  - Replace <PASSWORD> with the password of the database user
  
- Configure alert Postgres host by setting 'host'

  ```yaml
  postgres:
    host: <DATABASE_HOST>
  ```
  - Replace <DATABASE_HOST> with the hostname of the database server
  
- Configure alert Postgres port by setting 'port'

  ```yaml
  postgres:
    port: <DATABASE_PORT>
  ```
  - Replace <DATABASE_PORT> with the port the database server is running on 
  
- Configure alert Postgres database name by setting 'databaseName'

  ```yaml
  postgres:
    databaseName: <DATABASE_NAME>
  ```
  - Replace <DATABASE_NAME> with the name of the database created in previous steps

- Configure Postgres admin username and password following the instructions outlined here: [Using External Database](#using-external-database)

### Database Admin User Password
We now require administrator access to configure the database correctly. The following section describes how to configure the administrator credentials for both internal and external database configurations.

#### Using Internal Database
  - While editing the alert values.yaml, in the postgres section edit the following configurations to set the admin user password of the postgres database.
  
    ```yaml
    postgres:
      adminUserName: postgres
      adminPassword: "<ADMIN_PASSWORD>"
    ```
    - Replace <ADMIN_PASSWORD> with the name of the postgres administrator password
        - Note: The adminUserName: value must remain "postgres" when using the internal postgres database.
    - Example:
    ```yaml
    postgres:
      adminUserName: postgres
      adminPassword: my_admin_password
    ```
    
#### Using External Database
  - Please configure your external database settings as described [External Postgres Database](#external-postgres-database)
  - While editing the alert values.yaml, in the postgres section edit the following configurations to set the admin user password of the postgres database.
  
    ```yaml
    postgres:
      adminUserName: "<ADMIN_USERNAME>"
      adminPassword: "<ADMIN_PASSWORD>"
    ```
      - Replace <ADMIN_USERNAME> with the name of the postgres administrator username
      - Replace <ADMIN_PASSWORD> with the name of the postgres administrator password
    - Example:
    ```yaml
    postgres:
      adminUserName: my_admin_username
      adminPassword: my_admin_password
    ```
  
### Installing with Black Duck
- Enable deployment with Black Duck by setting 'deployAlertWithBlackDuck'

  ```yaml
  deployAlertWithBlackDuck: true
  ```
- Configure the Black Duck release name by setting 'blackDuckName'
  
  ```yaml
  blackDuckName: "<BLACKDUCK_RELEASE_NAME>"
  ```
  - Replace <BLACKDUCK_RELEASE_NAME> with the name of the release of Black Duck
  
- Configure the Black Duck namespace by setting 'blackDuckNamespace'

  ```yaml
  blackDuckNamespace: "<BLACK_DUCK_NAMESPACE>"
  ```
  - Replace <BLACK_DUCK_NAMESPACE> with the namespace where the Black Duck product deployed to
