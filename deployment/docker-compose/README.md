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

1. Create ALERT_ENCRYPTION_PASSWORD secret.
2. Create ALERT_ENCRYPTION_GLOBAL_SALT secret.
3. Create any optional secrets.
4. Modify environment variables.
5. Bring the containers up.
 
#### Details 
This section walks through each step of the installation procedure.

##### 1. Create ALERT_ENCRYPTION_PASSWORD secret.
  
- Create a docker secret containing the encryption password for Alert.

    ```docker secret create ALERT_ENCRYPTION_PASSWORD <FILE_CONTAINING_PASSWORD>```

- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file.
- Uncomment the following from the docker-compose.local-overrides.yml file alert service section.
    ```
        alert:
            secrets:
                - ALERT_ENCRYPTION_PASSWORD
    ```
- Uncomment the following from the secrets section of the docker-compose.local-overrides.yml file.
    ```
        secrets:
            ALERT_ENCRYPTION_PASSWORD:
              external:
                name: "ALERT_ENCRYPTION_PASSWORD"
            
    ```
##### 2. Create ALERT_ENCRYPTION_GLOBAL_SALT secret.

- Create a docker secret containing the encryption salt for Alert.

    ```docker secret create ALERT_ENCRYPTION_GLOBAL_SALT - <FILE_CONTAINING_SALT>```
    
- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file.
- Uncomment the following from the docker-compose.local-overrides.yml file alert service section.
    ```
        alert:
            secrets:
                - ALERT_ENCRYPTION_PASSWORD
                - ALERT_ENCRYPTION_GLOBAL_SALT
    ```
- Uncomment the following from the secrets section of the docker-compose.local-overrides.yml file.
    ```
        secrets:
            ALERT_ENCRYPTION_PASSWORD:
              external:
                name: "ALERT_ENCRYPTION_PASSWORD"
            ALERT_ENCRYPTION_GLOBAL_SALT:
              external:
                name: "ALERT_ENCRYPTION_GLOBAL_SALT"
            
    ```

##### 3. Create optional secrets.
- Using custom certificate for Alert web server. See [Using Custom Certificates](#using-custom-certificates)
- Using custom trust store to trust certificates of external servers. See [Using Custom Certificate TrustStore](#using-custom-certificate-truststore)

#### 4. Modify environment variables.
- Set the environment variables for your application. See [Environment Variables](#environment-variables)

##### 5. Bring the containers up.
- Start the containers 
    ```
    docker-compose -f ./docker-compose/docker-compose.yml -f ./docker-compose/docker-compose.local-overrides.yml -p <PROFILE_NAME> up -d
    ```
- Be sure to replace <PROFILE_NAME> with the profile name you wish to use for your deployment i.e. blackduck.
    ```
    docker-compose -f ./docker-compose/docker-compose.yml -f ./docker-compose.local-overrides.yml -p blackduck up -d
    ```
    
    Note: Don't forget the -d option at the end of the command line to run the command as a daemon process otherwise the container logs will go to standard output and ```ctrl+c``` will stop the application.
    
### Installation with Black Duck
Overview:
1. Create ALERT_ENCRYPTION_PASSWORD secret.
2. Create ALERT_ENCRYPTION_GLOBAL_SALT secret.
3. Create any optional secrets.
4. Modify environment variables.
5. Update the Black Duck installation to set the USE_ALERT environment variable for the NGinX container.
6. Install Black Duck. Follow the documented installation procedure for Black Duck.
7. Bring the containers up.

#### Details 
This section walks through each step of the installation procedure.

##### 1. Create ALERT_ENCRYPTION_PASSWORD secret.
  
- Create a docker secret containing the encryption password for Alert.

    ```docker secret create ALERT_ENCRYPTION_PASSWORD <FILE_CONTAINING_PASSWORD>```

- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file.
- Uncomment the following from the docker-compose.local-overrides.yml file alert service section.
    ```
        alert:
            secrets:
                - ALERT_ENCRYPTION_PASSWORD
    ```
- Uncomment the following from the secrets section of the docker-compose.local-overrides.yml file.
    ```
        secrets:
            ALERT_ENCRYPTION_PASSWORD:
              external:
                name: "ALERT_ENCRYPTION_PASSWORD"
            
    ```
##### 2. Create ALERT_ENCRYPTION_GLOBAL_SALT secret.

- Create a docker secret containing the encryption salt for Alert.

    ```docker secret create ALERT_ENCRYPTION_GLOBAL_SALT - <FILE_CONTAINING_SALT>```
    
- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file.
- Uncomment the following from the docker-compose.local-overrides.yml file to the alert service section.
    ```
        alert:
            secrets:
                - ALERT_ENCRYPTION_PASSWORD
                - ALERT_ENCRYPTION_GLOBAL_SALT
    ```
- Uncomment the following from the secrets section of the docker-compose.local-overrides.yml file.
    ```
        secrets:
            ALERT_ENCRYPTION_PASSWORD:
              external:
                name: "ALERT_ENCRYPTION_PASSWORD"
            ALERT_ENCRYPTION_GLOBAL_SALT:
              external:
                name: "ALERT_ENCRYPTION_GLOBAL_SALT"
            
    ```

##### 3. Create optional secrets.
- Using custom certificate for Alert web server. See [Using Custom Certificates](#using-custom-certificates)
- Using custom trust store to trust certificates of external servers. See [Using Custom Certificate TrustStore](#using-custom-certificate-truststore)

#### 4. Modify environment variables.
- Set the environment variables for your application. See [Environment Variables](#environment-variables)
##### 5. Update the Black Duck installation to set the USE_ALERT environment variable for the NGinX container.
For the NGinX container set the variable: ```USE_ALERT=1``` 
##### 6. Install Black Duck.
- Follow the installation procedure for installing Black Duck. Do not start the containers.  Skip that step.
##### 7. Bring the containers up.
- Copy the blackduck-alert.env file to the same location where the Black Duck docker-compose files are located.
- Start the containers. 
    ```
    docker-compose -f <PATH_TO_BLACK_DUCK>/docker-compose/docker-compose.yml -f <PATH_TO_ALERT>/docker-compose/docker-compose.yml -f <PATH_TO_ALERT>/docker-compose/docker-compose.local-overrides.yml -p <PROFILE_NAME> up -d
    ```
- Be sure to replace <PROFILE_NAME> with the profile name you wish to use for your deployment i.e. blackduck.
    ```
    docker-compose -f <PATH_TO_BLACK_DUCK>/docker-compose/docker-compose.yml -f <PATH_TO_ALERT>/docker-compose/docker-compose.yml -f <PATH_TO_ALERT>/docker-compose/docker-compose.local-overrides.yml -p blackduck up -d
    ```
    
    Note: Don't forget the -d option at the end of the command line to run the command as a daemon process otherwise the container logs will go to standard output and ```ctrl+c``` will stop the application.
    

## Upgrading Alert
You will remove the stack and then re-deploy your stack.
The steps in the upgrade procedure are the same as the installation procedure after removing the stack.

### Verify Secrets 
1. Review the docker secrets.

    ```docker secret ls```

### Standalone Upgrade
1. Run ```docker stack rm <STACK_NAME>```
2. Follow the [Standalone Installation](#standalone-installation)

### Upgrade with Black Duck
1. Run ```docker stack rm <STACK_NAME>```
2. Follow [Installation with Black Duck](#installation-with-black-duck)

## Optional Secrets 
This section describes how to configure some of the optional secrets. 

### Using Custom Certificates 
- Custom Certificates for the Alert Web server to present to clients.

    - Before you can use custom certificates for Alert you must have the signed certificate and key used to generate the certificate.

        - WEBSERVER_CUSTOM_CERT_FILE - The file containing the customer's signed certificate.
    
            ```docker secret create WEBSERVER_CUSTOM_CERT_FILE file <PATH_TO_CERT_FILE>```

        - WEBSERVER_CUSTOM_KEY_FILE - The file containing the customer's key used to create the certificate.

            ```docker secret create WEBSERVER_CUSTOM_KEY_FILE file <PATH_TO_KEY_FILE>```
    - Uncomment the following secrets from the docker-compose.local-overrides.yml file alert service section.
        ```
            alert:
                secrets:
                    - WEBSERVER_CUSTOM_CERT_FILE
                    - WEBSERVER_CUSTOM_KEY_FILE
        ```
    - Uncomment the following secrets from the secrets section of the docker-compose.local-overrides.yml file.
        ```
            secrets:
                WEBSERVER_CUSTOM_CERT_FILE:
                    external:
                        name: "WEBSERVER_CUSTOM_CERT_FILE"
                WEBSERVER_CUSTOM_KEY_FILE:
                    external:
                        name: "WEBSERVER_CUSTOM_KEY_FILE"
        ```
### Using Custom Certificate TrustStore
- Custom java trust store file for the Alert server to communicate over SSL to external systems.

    You must have a valid JKS trust store file that can be used as the Trust Store for Alert.  
    
    Only one of the following secrets needs to be created.  If both are created, then jssecacerts secret will take precedence and be used by Alert.

    - Create the secret.  Only create one of the following secrets.
        - jssecacerts - The java trust store file with any custom certificates imported.
    
            ```docker secret create jssecacerts file <PATH_TO_TRUST_STORE_FILE>```
    
            or 
    
        - cacerts - The java trust store file with any custom certificates imported. 
     
            ```docker secret create cacerts file <PATH_TO_TRUST_STORE_FILE>```
    - Uncomment the following from the docker-compose.local-overrides.yml file from the alert service section.

## Environment Variables 
Alert supports configuration of the application's components via environment variables.  There are two ways to configure the environment variables.
1. Edit the blackduck-alert.env file which is the easiest and most straight forward approach for Alert.
2. Edit the docker-compose.local-overrides.yml file to include the environment variables.

### Edit Environment File
Environment variables for Alert have already been created in this file but they are commented out.  
Uncomment the variable you wish to set by deleting the '#' character at the beginning of each line and set its value.

### Edit the Overrides File
Uncomment environment from the alert service section of docker-compose.local-overrides.yml.  
Add environment variables as <VARIABLE>: <VALUE> into the ```environment: {}``` section of the alert service.

### Environment Variable Overrides
The environment variables will always take precedence and overwrite the values stored in the database if the following variable value is set to 'true'.

```ALERT_COMPONENT_SETTINGS_SETTINGS_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE=true```

### Environment Variable Classifications
There are certain classifications with the environment variables. The variables have a specific naming convention:
```ALERT_<CLASSIFICATION>_<ITEM_NAME>_<CONFIGURATION_PROPERTY>```
- Provider:  The environment variables to configure these components start with ALERT_PROVIDER_
- Channel: The environment variables to configure these components start with ALERT_CHANNEL_
- Component: The environment variables to configure these components start with ALERT_COMPONENT_

Examples:
These are some examples of what can be set. the blackduck-alert.env file has a more comprehensive list.
- Provider: 
    - ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_URL= The URL for the Black Duck server.
- Channel:
    - ALERT_CHANNEL_JIRA_CLOUD_JIRA_CLOUD_URL= The URL for the Jira Cloud server.
    - ALERT_CHANNEL_EMAIL_MAIL_SMTP_HOST= The SMTP host used to send email messages.
- Component: 
    - ALERT_COMPONENT_SETTINGS_SETTINGS_LDAP_ENABLED= Boolean to determine if LDAP authentication is used.
