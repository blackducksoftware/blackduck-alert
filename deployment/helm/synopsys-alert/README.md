# alert-helm [alpha]
Helm Charts for Synopsys Alert

**Alpha Release**  
* This helm chart is in early testing and is not fully supported.  
* Some testing has been performed with Helm 2 releases. 

## Prerequisites

- Kubernetes 1.9+
- Helm2 or Helm3

## Installing the Chart -- Helm 2

#### Create the Namespace
```console
$ kubectl create ns <namespace>
```
#### Configure your Alert Instance
Modify the values.yaml file or pass in values to `helm intsall` with --set.  

#### Install the Alert Chart
```
$ helm install . --name <name> --namespace <namespace>
```

> **Tip**: List all releases using `helm list`


## Installing the Chart -- Helm 3

#### Create the Namespace and TLS Secrets
```console
$ kubectl create ns <namespace>
```
#### Configure your Black Duck Instance
Modify the values.yaml file or pass in values to `helm install` with --set.  

#### Install the Black Duck Chart
```
$ helm install <name> . --namespace <namespace>
```

## Quick Start with Helm 3
#### Step 1
Navigate to the alert-helm chart repository in your terminal
```
$ cd <path>/alert-helm
```

#### Step 2
```
$ kubectl create ns myalert
```

#### Step 3
Deploy Alert
```
$ helm install myalert . --namespace myalert
```

## Upgrading the Chart

```console
$ helm upgrade <name> . --namespace <namespace>
```

## Uninstalling the Chart

To uninstall/delete the deployment:

```console
$ helm delete <name> 
```

The command removes all the Kubernetes components associated with the chart and deletes the release.

## Configuration

The following table lists the configurable parameters of the Alert chart and their default values.

#### Common Configuration
| Parameter | Description | Default |
| --------- | ----------- | ------- |
| `alert.image` | image for the Alert container | `docker.io/blackducksoftware/blackduck-alert:VERSION_TOKEN` |
| `alert.limitMemory` | Alert container Memory Limit | `2560M` |
| `alert.requestMemory` | Alert container Memory Request | `2560M` |
| `alert.nodeSelector` | Alert node labels for pod assignment | `{}` | 
| `alert.tolerations` | Alert node tolerations for pod assignment | `[]` |
| `alert.affinity` | Alert node affinity for pod assignment | `{}` |
| `cfssl.image` | Image for the Cfssl container | `docker.io/blackducksoftware/blackduck-cfssl:1.0.0` |
| `cfssl.limitMemory` | Cfssl container Memory Limit | `640M` |
| `cfssl.requestMemory` | Cfssl container Request Limit | `640M` |
| `cfssl.nodeSelector` | Cfssl node labels for pod assigment | `{}` |
| `cfssl.tolerations` | Cfssl node tolerations for pod assignment | `[]` |
| `cfssl.affinity` | Cfssl node affinity for pod assignment | `{}` |
| `postgres.registry` |  Postgres registry containing image for the container | `"docker.io/centos"` |
| `postgres.isExternal` |  If true, do not deploy a Postgres container |  `false` |
| `postgres.host` |  Host name of the Postgres database | `""` |
| `postgres.port` |  Port of the Postgres database | `5432` |
| `postgres.userUserName` | Postgres database user owning the database Alert uses | `sa` |
| `postgres.userPassword` | Postgres database password for the user | `blackduck` |
| `postgres.databaseName` | Postgres database name where Alert data will be stored | `alertdb` |
| `postgres.persistentVolumeClaimName` | Postgres node volume claim name | `""` | 
| `postgres.claimSize` | Postgres node volume claim size | `"5Gi"` |
| `postgres.storageClass` | Postgres node storage class for volume claim | `""` |
| `postgres.volumeName` | Postgres node volume name for pod assignment | `""` |
| `postgres.nodeSelector` | Postgres node labels for pod assignment | `{}` |
| `postgres.tolerations` | Postgres node tolerations for pod assignment | `[]` |
| `postgres.affinity` | Postgres node affinity for pod assignment | `{}` |
| `postgres.podSecurityContext` | Postgres node pod security context | `{}` |
| `postgres.securityContext` | Postgres node security context | `{}` |
| `deployAlertWithBlackDuck` | If true, Alert will be configured to run with a Black Duck instance | `false` |
| `enableCertificateSecret` | if true, Alert will use values in a Secret for authenticating it's certificates | `false` |
| `enableStandalone` | if true, Alert will be deployed with it's own cfssl instance | `true` |
| `enablePersistentStorage` | if true, Alert will have persistent storage | `false` |
| `environs` | environment variables for the Alert container | `[]` |
| `exposeui` | if true, a Service to expose the UI will be created | `true` |
| `exposedServiceType` | type of exposed Service | `NodePort` |
| `pvcSize` | Persistent Volume Claim claim size | `5G` |
| `secretEnvirons` | sensitive environment variables for the Alert container to be stored in a Secret | `[]` |
| `setEncryptionSecretData` | if true, you will be prompted to set values for encrypting Alert's data | `false` |
| `storageClassName` | Persistent Volume Claim storage class | `""` |


Specify each parameter using the `--set key=value[,key=value]` argument to `helm install`.

Alternatively, a YAML file that specifies the values for the above parameters can be provided while installing the chart. For example,

```console
$ helm install . --name <name> --namespace <namespace> --set enableStandalone=true
```
