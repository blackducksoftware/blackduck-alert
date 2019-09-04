# Black Duck Alert On Docker Compose

This document describes how to install and upgrade Alert in Docker Compose.
Black Duck will no longer be supporting installations via Docker Compose in December 2019.  
This installation method is deprecated and will not be supported after December 2019.

## Table Of Contents
- [Requirements](#requirements)
- [Installing Alert](#installing-alert)
    - [Standalone Installation](#standalone-installation)
    - [Installation With Black Duck](#installation-with-black-duck)
- [Upgrading Alert](#upgrading-alert)
    - [Standalone Upgrade](#standalone-upgrade)
    - [Upgrade With Black Duck](#upgrade-with-black-duck)
- [Certificates](#certificates)
- [Environment Variables](#environment-variables) 
    - [Edit Environment File](#edit-environment-file)
    - [Edit the Overrides File](#edit-the-overrides-file)
    - [Environment Variable Overrides](#environment-variable-overrides)
    - [Alert Hostname Variable](#alert-hostname-variable)
    - [Alert Logging Level Variable](#alert-logging-level-variable)
    - [Email Channel Environment Variables](#email-channel-environment-variables)
    - [Environment Variable Classifications](#environment-variable-classifications)
- [Advanced Configuration](#advanced-configuration)
    - [Changing Server Port](#changing-server-port)
    - [Changing Memory Settings](#changing-memory-settings)

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
1. Create a directory for secrets.
2. Create ALERT_ENCRYPTION_PASSWORD file.
3. Create ALERT_ENCRYPTION_GLOBAL_SALT file.
4. Manage certificates.
5. Modify environment variables.
6. Bring the containers up.
 
#### Details 
This section walks through each step of the installation procedure.

##### 1. Create a directory for secrets.

- Create a directory to store secrets.
    ```
    mkdir -p <PATH>
    ```
    
    Example: 
    ```
    mkdir -p /alert/mysecrets
    ```
    
- Uncomment the following from the docker-compose.local-overrides.yml file alert service section.
    ```
      volumes: ['<PATH_TO_SECRETS>:/run/secrets']
    ```
      
- Replace <PATH_TO_SECRETS> of the docker-compose.local-overrides.yml file with the directory just created.
    
    Example:
    ```
        volumes:['/alert/mysecrets:/run/secrets']
    ```
##### 2. Create ALERT_ENCRYPTION_PASSWORD file.
  
- Create a file containing the encryption password for Alert in the secrets directory.

    ```
    echo "<PASSSWORD_TEXT>" >> <PATH_TO_SECRETS>/ALERT_ENCRYPTION_PASSWORD
    ```

##### 3. Create ALERT_ENCRYPTION_GLOBAL_SALT file.

- Create a file containing the encryption salt for Alert in the secrets directory.

    ```
    echo "<SALT_TEXT>" >> <PATH_TO_SECRETS>/ALERT_ENCRYPTION_GLOBAL_SALT
    ```

##### 4. Manage certificates.
This is an optional step. Confirm if custom certificates or a certificate store need to be used.
- Using custom certificate for Alert web server. See [Using Custom Certificates](#using-custom-certificates)
- Using custom trust store to trust certificates of external servers. See [Using Custom Certificate TrustStore](#using-custom-certificate-truststore)

#### 5. Modify environment variables.
Please see [Environment Variables](#environment-variables)
- Set the required environment variable ALERT_HOSTNAME. See [Alert Hostname Variable](#alert-hostname-variable)
- Set any other optional environment variables as needed.

##### 6. Bring the containers up.
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
1. Create a directory for secrets.
2. Create ALERT_ENCRYPTION_PASSWORD file.
3. Create ALERT_ENCRYPTION_GLOBAL_SALT file.
4. Manage certificates.
5. Modify environment variables.
6. Update the Black Duck installation to set the USE_ALERT environment variable for the NGinX container.
7. Install Black Duck. Follow the documented installation procedure for Black Duck.
8. Bring the containers up.

#### Details 
This section walks through each step of the installation procedure.

##### 1. Create a directory for secrets.

- Create a directory to store secrets.
    ```
    mkdir -p <PATH>
    ```
    
    Example: 
    ```
    mkdir -p /alert/mysecrets
    ```
    
- Uncomment the following from the docker-compose.local-overrides.yml file alert service section.
    ```
      volumes: ['<PATH_TO_SECRETS>:/run/secrets']
    ```
      
- Replace <PATH_TO_SECRETS> of the docker-compose.local-overrides.yml file with the directory just created.
    
    Example:
    ```
        volumes:['/alert/mysecrets:/run/secrets']
    ```
##### 2. Create ALERT_ENCRYPTION_PASSWORD file.
  
- Create a file containing the encryption password for Alert in the secrets directory.

    ```
    echo "<PASSSWORD_TEXT>" >> <PATH_TO_SECRETS>/ALERT_ENCRYPTION_PASSWORD
    ```

##### 3. Create ALERT_ENCRYPTION_GLOBAL_SALT file.

- Create a file containing the encryption salt for Alert in the secrets directory.

    ```
    echo "<SALT_TEXT>" >> <PATH_TO_SECRETS>/ALERT_ENCRYPTION_GLOBAL_SALT
    ```

##### 4. Manage certificates.
This is an optional step. Confirm if custom certificates or a certificate store need to be used.
- Using custom certificate for Alert web server. See [Using Custom Certificates](#using-custom-certificates)
- Using custom trust store to trust certificates of external servers. See [Using Custom Certificate TrustStore](#using-custom-certificate-truststore)

#### 5. Modify environment variables.
Please see [Environment Variables](#environment-variables)
- Set the required environment variable ALERT_HOSTNAME. See [Alert Hostname Variable](#alert-hostname-variable)
- Set any other optional environment variables as needed.

##### 6. Update the Black Duck installation to set the USE_ALERT environment variable for the NGinX container.
For the NGinX container set the variable: ```USE_ALERT=1``` 

##### 7. Install Black Duck.
- Follow the installation procedure for installing Black Duck. Do not start the containers.  Skip that step.

##### 8. Bring the containers up.
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
You will bring down the profile and then re-deploy the profile.
The steps in the upgrade procedure are the same as the installation procedure after bringing the profile down.

### Standalone Upgrade
1. Execute the command:  
    ```
    docker-compose -f ./docker-compose/docker-compose.yml -f ./docker-compose/docker-compose.local-overrides.yml -p <PROFILE_NAME> down
    ```
2. Follow the [Standalone Installation](#standalone-installation)

### Upgrade with Black Duck
1. Execute the command:
    ```
    docker-compose -f <PATH_TO_BLACK_DUCK>/docker-compose/docker-compose.yml -f <PATH_TO_ALERT>/docker-compose/docker-compose.yml -f <PATH_TO_ALERT>/docker-compose/docker-compose.local-overrides.yml -p <PROFILE_NAME> down
    ```
2. Follow [Installation with Black Duck](#installation-with-black-duck)

## Certificates 
This section describes how to configure the optional certificates.  Please verify beforehand if custom certificates or a certificate truststore must be used.

### Using Custom Certificates 
- Custom Certificates for the Alert web server to present to clients.

    - Before you can use custom certificates for Alert you must have the signed certificate and key used to generate the certificate.

        - WEBSERVER_CUSTOM_CERT_FILE - The file containing the customer's signed certificate.
    
            ```cp <PATH_TO_CERT_FILE> <PATH_TO_SECRETS>/WEBSERVER_CUSTOM_CERT_FILE```

        - WEBSERVER_CUSTOM_KEY_FILE - The file containing the customer's key used to create the certificate.

            ```cp <PATH_TO_KEY_FILE> <PATH_TO_SECRETS>/WEBSERVER_CUSTOM_KEY_FILE```
            
### Using Custom Certificate TrustStore
- Custom java trust store file for the Alert server to communicate over SSL to external systems.

    You must have a valid JKS trust store file that can be used as the Trust Store for Alert.  
    
    Only one of the following secrets needs to be created.  If both are created, then jssecacerts secret will take precedence and be used by Alert.

    - Create the secret.  Only create one of the following secrets.
        - jssecacerts - The java trust store file with any custom certificates imported.
    
            ```cp <PATH_TO_TRUST_STORE_FILE> <PATH_TO_SECRETS>/jssecacerts```
    
            or 
    
        - cacerts - The java trust store file with any custom certificates imported. 
     
            ```cp <PATH_TO_TRUST_STORE_FILE> <PATH_TO_SECRETS>/cacerts```

## Environment Variables 
Alert supports configuration of the application's components via environment variables.  There are two ways to configure the environment variables.
1. Edit the blackduck-alert.env file.
2. Edit the docker-compose.local-overrides.yml file to include the environment variables.

Note: You will need to edit to docker-compose.local-overrides.yml file for other settings.  
When installing choose to either edit: 

```docker-compose.local-overrides.yml``` 

or 

```docker-compose.local.overrides.yml``` and ```blackduck-alert.env```

### Edit Environment File
Environment variables for Alert have already been created in this file but they are commented out.  
Uncomment the variable you wish to set by deleting the '#' character at the beginning of each line and set its value.

Example: 
```
ALERT_HOSTNAME=localhost
ALERT_LOGGING_LEVEL=INFO
```

### Edit the Overrides File
Uncomment environment from the alert service section of docker-compose.local-overrides.yml.  
Add environment variables as ```- <VARIABLE_NAME>=<VARIABLE_VALUE>``` into the ```environment: ``` section of the alert service.

Example: 
```
    environment:
        - ALERT_HOSTNAME=localhost
        - ALERT_LOGGING_LEVEL=INFO
```

### Environment Variable Overrides
The environment variables will always take precedence and overwrite the values stored in the database if the following variable value is set to 'true'.

```ALERT_COMPONENT_SETTINGS_SETTINGS_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE=true```

### Alert Hostname Variable
You must specify the ALERT_HOSTNAME environment variable in order for Alert to generate and use certificates correctly.
- Add the ALERT_HOSTNAME environment variable the value must be the hostname only 
    - Editing environment file:
        ```
        ALERT_HOSTNAME=<NEW_HOST_NAME>
        ```
    - Editing overrides file:
        ```
            environment:
                - ALERT_HOSTNAME=<NEW_HOST_NAME>
        ```
- Do not add the protocol a.k.a scheme to the value of the variable.
    - Good: ```ALERT_HOSTNAME=myhost.example.com```
    - Bad: ```ALERT_HOSTNAME=https://myhost.example.com```

### Alert Logging Level Variable
To change the logging level of alert add the following environment variable to your deployment. 

- Editing environment file: 
    ```ALERT_LOGGING_LEVEL=DEBUG```
- Editing overrides file: 
    ```
        environment: 
           - ALERT_LOGGING_LEVEL=DEBUG
    ```

- Set the value to one of the following: 
    - DEBUG
    - ERROR
    - INFO
    - TRACE
    - WARN

### Email Channel Environment Variables
A majority of the Email Channel environment variables that can be set are related to JavaMail configuration properties.  You can find the JavaMail properties here: 

[JavaMail Properties](https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html)

- The Email Channel environment variables have a prefix of ```ALERT_CHANNEL_EMAIL_```
- The remaining portion of the variable, after the prefix, map to the JavaMail properties if you replace '_' with '.'

Examples:
- ALERT_CHANNEL_EMAIL_MAIL_SMTP_HOST maps to 'mail.smtp.host'
- ALERT_CHANNEL_EMAIL_MAIL_SMTP_PORT maps to 'mail.smtp.port'

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

## Advanced Configuration
This section describes some advanced configuration settings for the Alert server.

### Changing Server Port
If Alert should not be running on it's default port of 8443, then this section describes what you have to change in order to use a different port.

For this advanced setting since there are more than just environment variables that need to be set this should be performed by editing the ```docker-compose.local-overrides.yml``` file.

- Overrides File Changes.
    - Define the new ports for the alert service.  Add 'ports' to the service description. 
    ```
        alert: 
            ports: ['<NEW_PORT>:<NEW_PORT>']
    ```
    - Define the ```ALERT_SERVER_PORT``` environment variable.
    ```
        alert: 
            environment:
                - ALERT_HOSTNAME=localhost
                - ALERT_SERVER_PORT=<NEW_PORT>
    ```
    - Define the healthcheck for the alert service. Add 'healthcheck' to the service description.
    ```
        alert:
            healthcheck:
                  test: [CMD, /usr/local/bin/docker-healthcheck.sh, 'https://localhost:<NEW_PORT>/alert/api/about',
                         /opt/blackduck/alert/security/root.crt, /opt/blackduck/alert/security/blackduck_system.crt,
                         /opt/blackduck/alert/security/blackduck_system.key]
                  interval: 30s
                  timeout: 60s
                  retries: 15
    ```
Example:
- Change the port to 9090 via the ```docker-compose.local-overrides.yml``` file.
```
    alert:
        ports: ['9090:9090']
        environment:
            - ALERT_HOSTNAME=localhost
            - ALERT_SERVER_PORT=9090
        healthcheck:
            test: [CMD, /usr/local/bin/docker-healthcheck.sh, 'https://localhost:9090/alert/api/about',
                 /opt/blackduck/alert/security/root.crt, /opt/blackduck/alert/security/blackduck_system.crt,
                /opt/blackduck/alert/security/blackduck_system.key]
            interval: 30s
            timeout: 60s
            retries: 15
```
  
Note: Work with your IT staff if necessary to verify the configured port is accessible through the network.

### Changing Memory Settings
If Alert should be using more memory than its default settings, then this section describes what you have to change in order to allocate more memory.

For this advanced setting since there are more than just environment variables that need to be set this should be performed by editing the ```docker-compose.local-overrides.yml``` file.

- Overrides File Changes.
    - Define the ```ALERT_MAX_HEAP_SIZE``` environment variable:
    ```
        alert:
            environment:
                - ALERT_HOSTNAME=localhost
                - ALERT_MAX_HEAP_SIZE=<NEW_HEAP_SIZE>
    ```
    - Define the container memory limit. Add 'mem_limit' to the service description.
    ```
        alert:
            mem_limit: <NEW_HEAP_SIZE + 256M>
    ```
    Note: 
        The ALERT_MAX_HEAP_SIZE and the container mem_limit settings should not be exactly the same.  
        The container mem_limit setting is the maximum memory allocated to the container.  
        Additional memory does not get allocated to it.  
        The maximum heap size in Java is the maximum size of the heap in the Java virtual machine (JVM), but the JVM also uses additional memory.  
        Therefore, the ALERT_MAX_HEAP_SIZE environment variable must be less than the amount defined in the mem_limit which is set for the container. 
        Synopsys recommends setting the mem_limit using the following formula: ALERT_MAX_HEAP_SIZE + 256M.
        
            ALERT_MAX_HEAP_SIZE = 4096M
            mem_limit = ALERT_MAX_HEAP_SIZE + 256M = 4096M + 256M = 4352M
                
Example: 
- Change the memory limit from 2G to 4G.
```
    alert:
        environment:
            - ALERT_HOSTNAME=localhost
            - ALERT_MAX_HEAP_SIZE=4096M
        mem_limit: 4352M
```

Note: Work with your IT staff if necessary to verify the configured memory is available on the host machine.
