# Black Duck Alert On Docker Swarm

This document describes how to install and upgrade Alert in Docker Swarm.

## Requirements

- You have a swarm node with at least 2GB of allocatable memory.
- You have administrative access to your docker host.  
- Before installing or upgrading Alert you must create desired persistent storage volumes for Alert.

## Installing Alert
Deployment files for Docker Swarm are located in the docker-swarm directory of the: 

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
4. Modify the environment variables.
6. Deploy the stack.

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
- ALERT_HOSTNAME: Set this to match the hostname to be used to access Alert.
    - Should be the same value as the value of the PUBLIC_HUB_WEBSERVER_HOST environment variable.
- ALERT_SERVER_PORT: If the default port of 8443 is not available or not to be used. 
    - Work with your IT staff if necessary to verify the the configured port is accessible through the network. 
##### 5. Deploy the stack.
- ```docker stack deploy -c <PATH>/docker-swarm/standalone/docker-compose.yml -c <PATH>/docker-swarm/docker-compose.local-overrides.yml blackduck```

### Installation with Black Duck
This section walks through the instructions to install Alert with a Black Duck instance.
#### Overview
1. Create ALERT_ENCRYPTION_PASSWORD secret. See [Required Secrets](#required-secrets).
2. Create ALERT_ENCRYPTION_GLOBAL_SALT secret. See [Required Secrets](#required-secrets).
3. Create any optional secrets. See [Optional Secrets](#optional-secrets).
4. Update the Black Duck installation to set the USE_ALERT environment variable for the NGinX container.
5. Install Black Duck following the documented installation procedure for Black Duck.
6. Modify the environment variables. 
7. Deploy the stack. 

#### Details
This section walks through each step of the installation procedure.

##### 1. Create ALERT_ENCRYPTION_PASSWORD secret. ####
- ```docker secret create <STACK_NAME>_ALERT_ENCRYPTION_PASSWORD - <PASSWORD_VALUE>```
##### 2. Create ALERT_ENCRYPTION_GLOBAL_SALT secret.
- ```docker secret create <STACK_NAME>_ALERT_ENCRYPTION_GLOBAL_SALT - <SALT_VALUE>```
##### 3. Create optional secrets.
##### 4. Update the Black Duck installation to set the USE_ALERT environment variable for the NGinX container.
- For the NGinX container set the variable: ```USE_ALERT=1``` 
##### 5. Install Black Duck following the documented installation procedure for Black Duck. 
##### 6. Modify environment variables.
- ALERT_HOSTNAME: Set this to match the hostname to be used to access Alert.
    - Should be the same value as the value of the PUBLIC_HUB_WEBSERVER_HOST environment variable.
- ALERT_SERVER_PORT: If the default port of 8443 is not available or not to be used. 
    - Work with your IT staff if necessary to verify the the configured port is accessible through the network. 
##### 7. Deploy the stack.
- ```docker stack deploy -c <PATH>/docker-swarm/hub/docker-compose.yml blackduck```
- Use the same stack name used to install Black Duck from step 4.


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
    
        ```docker secret create <STACK_NAME>_ALERT_ENCRYPTION_PASSWORD - <PASSWORD_VALUE>```

    - ALERT_ENCRYPTION_GLOBAL_SALT - The encryption salt string. Recommended to be longer than at least 8 characters.
    
        ```docker secret create <STACK_NAME>_ALERT_ENCRYPTION_GLOBAL_SALT - <SALT_VALUE>```

### Optional Secrets 
- Custom Certificates for the Alert Web server to present to clients.

    Before you can use custom certificates for Alert you must have the signed certificate and key used to generate the certificate.

    - WEBSERVER_CUSTOM_CERT_FILE - The file containing the customer's signed certificate.
    
        ```docker secret create <STACK_NAME>_WEBSERVER_CUSTOM_CERT_FILE file <PATH_TO_CERT_FILE>```

    - WEBSERVER_CUSTOM_KEY_FILE - The file containing the customer's key used to create the certificate.

        ```docker secret create <STACK_NAME>_WEBSERVER_CUSTOM_KEY_FILE file <PATH_TO_KEY_FILE>```

- Custom java trust store file for the Alert server to communicate over SSL to external systems.

    You must have a valid JKS trust store file that can be used as the Trust Store for Alert.  
    
    Only one of the following secrets needs to be created.  If both are created, then jssecacerts secret will take precedence and be used by Alert.

    - jssecacerts - The java trust store file with any custom certificates imported.

        ```docker secret create <STACK_NAME>_jssecacerts file <PATH_TO_TRUST_STORE_FILE>```

        or 

    - cacerts - The java trust store file with any custom certificates imported. 
 
        ```docker secret create <STACK_NAME>_cacerts file <PATH_TO_TRUST_STORE_FILE>```





