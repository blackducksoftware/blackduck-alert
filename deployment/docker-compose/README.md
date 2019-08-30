# Black Duck Alert On Docker Compose

This document describes how to install and upgrade Alert in Docker Compose.
Black Duck will no longer be supporting installations via Docker Compose in December 2019.  
This installation method is deprecated and will not be supported after December 2019.


## Requirements

- You have a Docker host with at least 2GB of allocatable memory.
- You have administrative access to your docker host.  

## Installing Alert
Deployment files for Docker Compose are located in the docker-compose directory of the: 

blackduck-alert-\<VERSION>\-deployment.zip file.

- Extract the contents of the ZIP file.
- For installing with the Black Duck the files are located in the hub sub-directory.
- For installing Alert standalone the files are located in the standalone sub-directory.

### Standalone Installation
This section walk through the instructions to install Alert in a standalone fashion.
#### Overview

1. Create ALERT_ENCRYPTION_PASSWORD secret. See [Required Secrets](#required-secrets).
2. Create ALERT_ENCRYPTION_GLOBAL_SALT secret. See [Required Secrets](#required-secrets).
3. Create any optional secrets. See [Optional Secrets](#optional-secrets).
4. Modify environment variables.
6. Bring the containers up.
 
#### Details 
This section walks through each step of the installation procedure.

##### 1. Create ALERT_ENCRYPTION_PASSWORD secret.
  
- Create a docker secret containing the encryption password for Alert.

    ```docker secret create <STACK_NAME>_ALERT_ENCRYPTION_PASSWORD <FILE_CONTAINING_PASSWORD>```

- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file
- Uncomment the following secret from the docker-compose.local-overrides.yml file to the alert service
    ```
        alert:
            secrets:
                - ALERT_ENCRYPTION_PASSWORD
    ```
- Uncomment the following secret from the secrets section of the docker-compose.local-overrides.yml file.
    ```
        secrets:
            ALERT_ENCRYPTION_PASSWORD
            external:
              name: "<STACK_NAME>_ALERT_ENCRYPTION_PASSWORD"
            
    ```
- Replace <STACK_NAME> with the stack name to be used in the deployment i.e. blackduck
    ```
        secrets:
            ALERT_ENCRYPTION_PASSWORD
            external:
              name: "blackduck_ALERT_ENCRYPTION_PASSWORD"
            
    ```
##### 2. Create ALERT_ENCRYPTION_GLOBAL_SALT secret.

- Create a docker secret containing the encryption salt for Alert.

    ```docker secret create <STACK_NAME>_ALERT_ENCRYPTION_GLOBAL_SALT - <FILE_CONTAINING_SALT>```
    
- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file
- Uncomment the following secret from the docker-compose.local-overrides.yml file to the alert service
    ```
        alert:
            secrets:
                - ALERT_ENCRYPTION_PASSWORD
                - ALERT_ENCRYPTION_GLOBAL_SALT
    ```
- Uncomment the following secret from the secrets section of the docker-compose.local-overrides.yml file.
    ```
        secrets:
            ALERT_ENCRYPTION_PASSWORD
              external:
                name: "<STACK_NAME>_ALERT_ENCRYPTION_PASSWORD"
            ALERT_ENCRYPTION_GLOBAL_SALT
              external:
                name: "<STACK_NAME>_ALERT_ENCRYPTION_GLOBAL_SALT"
            
    ```
- Replace <STACK_NAME> with the stack name to be used in the deployment i.e. blackduck
    ```
        secrets:
            ALERT_ENCRYPTION_PASSWORD
              external:
                name: "blackduck_ALERT_ENCRYPTION_PASSWORD"
            ALERT_ENCRYPTION_GLOBAL_SALT
              external:
                name: "blackduck_ALERT_ENCRYPTION_GLOBAL_SALT"
                
    ```

##### 3. Create optional secrets.
##### 4. Modify environment variables.
##### 5. Bring the containers up.

### Installation with Black Duck
Overview:
1. Create ALERT_ENCRYPTION_PASSWORD secret. See [Required Secrets](#required-secrets).
2. Create ALERT_ENCRYPTION_GLOBAL_SALT secret. See [Required Secrets](#required-secrets).
3. Create any optional secrets. See [Optional Secrets](#optional-secrets).
4. Update the Black Duck installation to set the USE_ALERT environment variable for the NGinX container.
5. Install Black Duck following the documented installation procedure for Black Duck.
6. Modify environment variables.
7. Deploy the stack. 

#### Details 
This section walks through each step of the installation procedure.

##### 1. Create ALERT_ENCRYPTION_PASSWORD secret.
  
- Create a docker secret containing the encryption password for Alert.

    ```docker secret create <STACK_NAME>_ALERT_ENCRYPTION_PASSWORD <FILE_CONTAINING_PASSWORD>```

- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file
- Uncomment the following secret from the docker-compose.local-overrides.yml file to the alert service
    ```
        alert:
            secrets:
                - ALERT_ENCRYPTION_PASSWORD
    ```
- Uncomment the following secret from the secrets section of the docker-compose.local-overrides.yml file.
    ```
        secrets:
            ALERT_ENCRYPTION_PASSWORD
            external:
              name: "<STACK_NAME>_ALERT_ENCRYPTION_PASSWORD"
            
    ```
- Replace <STACK_NAME> with the stack name to be used in the deployment i.e. blackduck
    ```
        secrets:
            ALERT_ENCRYPTION_PASSWORD
            external:
              name: "blackduck_ALERT_ENCRYPTION_PASSWORD"
            
    ```
##### 2. Create ALERT_ENCRYPTION_GLOBAL_SALT secret.

- Create a docker secret containing the encryption salt for Alert.

    ```docker secret create <STACK_NAME>_ALERT_ENCRYPTION_GLOBAL_SALT - <FILE_CONTAINING_SALT>```
    
- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file
- Uncomment the following secret from the docker-compose.local-overrides.yml file to the alert service
    ```
        alert:
            secrets:
                - ALERT_ENCRYPTION_PASSWORD
                - ALERT_ENCRYPTION_GLOBAL_SALT
    ```
- Uncomment the following secret from the secrets section of the docker-compose.local-overrides.yml file.
    ```
        secrets:
            ALERT_ENCRYPTION_PASSWORD
              external:
                name: "<STACK_NAME>_ALERT_ENCRYPTION_PASSWORD"
            ALERT_ENCRYPTION_GLOBAL_SALT
              external:
                name: "<STACK_NAME>_ALERT_ENCRYPTION_GLOBAL_SALT"
            
    ```
- Replace <STACK_NAME> with the stack name to be used in the deployment i.e. blackduck
    ```
        secrets:
            ALERT_ENCRYPTION_PASSWORD
              external:
                name: "blackduck_ALERT_ENCRYPTION_PASSWORD"
            ALERT_ENCRYPTION_GLOBAL_SALT
              external:
                name: "blackduck_ALERT_ENCRYPTION_GLOBAL_SALT"
                
    ```

##### 3. Create optional secrets.
##### 4. Update the Black Duck installation to set the USE_ALERT environment variable for the NGinX container.
For the NGinX container set the variable: ```USE_ALERT=1``` 
##### 5. Modify environment variables.
##### 6. Bring the containers up.

## Upgrading Alert
You will remove the stack and then re-deploy your stack.
The steps in the upgrade procedure are the same as the installation procedure after removing the stack.

### Verify Secrets 
1. Review the docker secrets.

    ```docker secret ls```
    
2. Create any docker secrets for the upgrade as described in [Docker Secrets](#docker-secrets)

### Standalone Upgrade
1. Run ```docker stack rm blackduck```
2. Follow the [Standalone Installation](#standalone-installation)

### Upgrade with Black Duck
1. Run ```docker stack rm blackduck```
2. Follow [Installation with Black Duck](#installation-with-black-duck)

## Docker Secrets
Alert uses docker secrets in order to protect configuration information that Alert needs.

### Required Secrets
- The items described in this section are required in order for Alert to start properly.

    - ALERT_ENCRYPTION_PASSWORD - The encryption password string. Recommended to be longer than at least 8 characters.
    
        ```docker secret create ALERT_ENCRYPTION_PASSWORD - <PASSWORD_VALUE>```

    - ALERT_ENCRYPTION_GLOBAL_SALT - The encryption salt string. Recommended to be longer than at least 8 characters.
    
        ```docker secret create ALERT_ENCRYPTION_GLOBAL_SALT - <SALT_VALUE>```

### Optional Secrets 
- Custom Certificates for the Alert Web server to present to clients.

    Before you can use custom certificates for Alert you must have the signed certificate and key used to generate the certificate.

    - WEBSERVER_CUSTOM_CERT_FILE - The file containing the customer's signed certificate.
    
        ```docker secret create WEBSERVER_CUSTOM_CERT_FILE file <PATH_TO_CERT_FILE>```

    - WEBSERVER_CUSTOM_KEY_FILE - The file containing the customer's key used to create the certificate.

        ```docker secret create WEBSERVER_CUSTOM_KEY_FILE file <PATH_TO_KEY_FILE>```

- Custom java trust store file for the Alert server to communicate over SSL to external systems.

    You must have a valid JKS trust store file that can be used as the Trust Store for Alert.  
    
    Only one of the following secrets needs to be created.  If both are created, then jssecacerts secret will take precedence and be used by Alert.

    - jssecacerts - The java trust store file with any custom certificates imported.

        ```docker secret create jssecacerts file <PATH_TO_TRUST_STORE_FILE>```

        or 

    - cacerts - The java trust store file with any custom certificates imported. 
 
        ```docker secret create WEBSERVER_CUSTOM_KEY_FILE file <PATH_TO_TRUST_STORE_FILE>```

## Environment Variables 

### Environment Variable Overrides
