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
    - [Using Custom Certificates](#using-custom-certificates)
    - [Using Custom Certificate Truststore](#using-custom-certificate-truststore)
- [Environment Variables](#environment-variables)
    - [Editing the Overrides File](#editing-the-overrides-file)
    - [Environment Variable Overrides](#environment-variable-overrides)
    - [Alert Hostname Variable](#alert-hostname-variable)
    - [Alert Logging Level Variable](#alert-logging-level-variable)
    - [Email Channel Environment Variables](#email-channel-environment-variables)
    - [Environment Variable Classifications](#environment-variable-classifications)
- [Advanced Configuration](#advanced-configuration)
    - [Changing Server Port](#changing-server-port)
    - [Changing Memory Settings](#changing-memory-settings)

## Requirements

- A Docker host with at least 2GB of allocatable memory.
- Administrative access to the docker host machine.  

## Installing Alert
Deployment files for Docker Compose are located in the *docker-compose* directory of the zip file.
```bash
blackduck-alert-<VERSION>-deployment.zip file.
```

- Extract the contents of the ZIP file.
- For installing with the Black Duck the files are located in the *hub* sub-directory.
- For installing Alert standalone the files are located in the *standalone* sub-directory.

### Standalone Installation
This section will walk through the instructions to install Alert in a standalone fashion.

#### Overview
1. Create a directory for secrets.
2. Create ALERT_ENCRYPTION_PASSWORD file.
3. Create ALERT_ENCRYPTION_GLOBAL_SALT file.
4. Manage certificates.
5. Modify environment variables.
6. Bring the containers up.

#### Details
This section will walk through each step of the installation procedure.

##### 1. Create a directory for secrets.

- Create a directory to store secrets.
    ```bash
    mkdir -p <PATH_TO_SECRETS>
    ```

    Example:
    ```bash
    mkdir -p /alert/mysecrets
    ```

- Uncomment the following from the docker-compose.local-overrides.yml file alert service section.
    ```yaml
      volumes: ['<PATH_TO_SECRETS>:/run/secrets']
    ```

- Replace <PATH_TO_SECRETS> of the docker-compose.local-overrides.yml file with the directory just created.

    Example:
    ```yaml
        volumes:['/alert/mysecrets:/run/secrets']
    ```
##### 2. Create ALERT_ENCRYPTION_PASSWORD file.

- Create a file containing the encryption password for Alert in the secrets directory.
    ```bash
    echo "<PASSWORD_TEXT>" >> <PATH_TO_SECRETS>/ALERT_ENCRYPTION_PASSWORD
    ```
    - Replace <PASSWORD_TEXT> with the password to use.
    - Replace <PATH_TO_SECRETS> with the path created in step 1 above.
    
    Note: With the command shown above ```echo``` will put the password in your shell history. 
    If you do not want the password in the shell history, then use a text editor like ```vi``` and create the ALERT_ENCRYPTION_PASSWORD file. 
    
##### 3. Create ALERT_ENCRYPTION_GLOBAL_SALT file.
- Create a file containing the encryption salt for Alert in the secrets directory.
    ```bash
    echo "<SALT_TEXT>" >> <PATH_TO_SECRETS>/ALERT_ENCRYPTION_GLOBAL_SALT
    ```
    - Replace <SALT_TEXT> with the salt to use.
    - Replace <PATH_TO_SECRETS> with the path created in step 1 above.
    
    Note: If you created the secret ALERT_ENCRYPTION_SALT in a version of Alert prior to 5.x, please rename the file to ALERT_ENCRYPTION_GLOBAL_SALT to use the same salt as the previous version.
    Also note, with the command shown above ```echo``` will put the salt in your shell history. 
    If you do not want the salt in the shell history, then use a text editor like ```vi``` and create the ALERT_ENCRYPTION_GLOBAL_SALT file.
    
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
    ```bash
    docker-compose -f <PATH>/docker-compose/standalone/docker-compose.yml -f <PATH>/docker-compose/docker-compose.local-overrides.yml -p <PROFILE_NAME> up -d
    ```
- Replace <PATH> with the path to the Alert installation files.
- Replace <PROFILE_NAME> with the profile name used for the deployment i.e. blackduck.
    ```bash
    docker-compose -f <PATH>/docker-compose/standalone/docker-compose.yml -f <PATH>/docker-compose.local-overrides.yml -p blackduck up -d
    ```

    Note: Don't forget the -d option at the end of the command line to run the command as a daemon process otherwise the container logs will go to standard output and ```ctrl+c``` will stop the application.

### Installation with Black Duck
This section will walk through the instructions to install Alert in a deployment with Black Duck.

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
This section will walk through each step of the installation procedure.  

**Please Note:** 
If you are upgrading Alert from a 4.x version to 5.x, please use the docker-compose.local-overrides.yml bundled with Alert.
Please remove any Alert configuration from the docker-compose.local-overrides.yml file bundled with Black Duck.

##### 1. Create a directory for secrets.

- Create a directory to store secrets.
    ```bash
    mkdir -p <PATH_TO_SECRETS>
    ```

    Example:
    ```bash
    mkdir -p /alert/mysecrets
    ```

- Uncomment the following from the docker-compose.local-overrides.yml file alert service section.
    ```yaml
      volumes: ['<PATH_TO_SECRETS>:/run/secrets']
    ```

- Replace <PATH_TO_SECRETS> of the docker-compose.local-overrides.yml file with the directory just created.

    Example:
    ```yaml
        volumes:['/alert/mysecrets:/run/secrets']
    ```
##### 2. Create ALERT_ENCRYPTION_PASSWORD file.

- Create a file containing the encryption password for Alert in the secrets directory.
    ```bash
    echo "<PASSWORD_TEXT>" >> <PATH_TO_SECRETS>/ALERT_ENCRYPTION_PASSWORD
    ```
    - Replace <PASSWORD_TEXT> with the password to use.
    - Replace <PATH_TO_SECRETS> with the path created in step 1 above.
        
##### 3. Create ALERT_ENCRYPTION_GLOBAL_SALT file.

- Create a file containing the encryption salt for Alert in the secrets directory.
    ```bash
    echo "<SALT_TEXT>" >> <PATH_TO_SECRETS>/ALERT_ENCRYPTION_GLOBAL_SALT
    ```
    - Replace <SALT_TEXT> with the salt to use.
    - Replace <PATH_TO_SECRETS> with the path created in step 1 above.
    
    Note: If you created the secret ALERT_ENCRYPTION_SALT in a version of Alert prior to 5.x, please rename the file to ALERT_ENCRYPTION_GLOBAL_SALT to use the same salt as the previous version.
    
##### 4. Manage certificates.
This is an optional step. Confirm if custom certificates or a certificate store need to be used.
- Using custom certificate for Alert web server. See [Using Custom Certificates](#using-custom-certificates)
- Set the required environment variable PUBLIC_HUB_WEBSERVER_HOST. See [Black Duck Web Server Host](#black-duck-web-server-host)
- Using custom trust store to trust certificates of external servers. See [Using Custom Certificate TrustStore](#using-custom-certificate-truststore)

#### 5. Modify environment variables.
Please see [Environment Variables](#environment-variables)
- Set the required environment variable ALERT_HOSTNAME. See [Alert Hostname Variable](#alert-hostname-variable)
- Set the required environment variable PUBLIC_HUB_WEBSERVER_HOST. See [Black Duck Web Server Host](#black-duck-web-server-host)
- Set any other optional environment variables as needed.

##### 6. Update the Black Duck installation to set the USE_ALERT environment variable for the NGinX container.
In the Black Duck deployment files set the following variable for the webserver container: 
```bash
USE_ALERT=1
```

##### 7. Install Black Duck.
- Follow the installation procedure for installing Black Duck. Do not start the containers. Skip the step to start the containers.

##### 8. Bring the containers up.
- Copy the blackduck-alert.env file to the same location where the Black Duck docker-compose files are located.
- Start the containers.
    ```bash
    docker-compose -f <PATH_TO_BLACK_DUCK>/docker-compose/docker-compose.yml -f <PATH_TO_BLACK_DUCK>/docker-compose/docker-compose.local-overrides.yml -f <PATH_TO_ALERT>/docker-compose/docker-compose.yml -f <PATH_TO_ALERT>/docker-compose/docker-compose.local-overrides.yml -p <PROFILE_NAME> up -d
    ```
    - Replace <PATH_TO_BLACK_DUCK> with the directory path to the Black Duck installation files.
    - Replace <PATH_TO_ALERT> with the directory path to the Alert installation files.
    - Replace <PROFILE_NAME> with the profile name used for the deployment i.e. blackduck.
    ```bash
    docker-compose -f <PATH_TO_BLACK_DUCK>/docker-compose/docker-compose.yml -f <PATH_TO_BLACK_DUCK>/docker-compose/docker-compose.local-overrides.yml -f <PATH_TO_ALERT>/docker-compose/docker-compose.yml -f <PATH_TO_ALERT>/docker-compose/docker-compose.local-overrides.yml -p blackduck up -d
    ```

    Note: Don't forget the -d option at the end of the command line to run the command as a daemon process otherwise the container logs will go to standard output and ```ctrl+c``` will stop the application.

## Upgrading Alert
Bring down the profile and then re-deploy the profile.
The steps in the upgrade procedure are the same as the installation procedure after bringing the profile down.

### Standalone Upgrade
1. Execute the command:  
    ```bash
    docker-compose -f <PATH>/docker-compose/standalone/docker-compose.yml -f <PATH>/docker-compose/docker-compose.local-overrides.yml -p <PROFILE_NAME> down
    ```
    - Replace <PROFILE_NAME> with the name of the profile that was used during installation.
    - Replace <PATH> with the path to the Alert installation files.
2. Follow the [Standalone Installation](#standalone-installation)

### Upgrade with Black Duck
1. Execute the command:
    ```bash
    docker-compose -f <PATH_TO_BLACK_DUCK>/docker-compose/docker-compose.yml -f <PATH_TO_BLACK_DUCK>/docker-compose/docker-compose.local-overrides.yml -f <PATH_TO_ALERT>/docker-compose/hub/docker-compose.yml -f <PATH_TO_ALERT>/docker-compose/docker-compose.local-overrides.yml -p <PROFILE_NAME> down
    ```
   - Replace <PATH_TO_BLACK_DUCK> with the directory path to the Black Duck installation files.
   - Replace <PATH_TO_ALERT> with the directory path to the Alert installation files.
   - Replace <PROFILE_NAME> with the name of the profile that was used during installation.
   
2. Follow [Installation with Black Duck](#installation-with-black-duck)

## Certificates
This section describes how to configure the optional certificates.  Please verify beforehand if custom certificates or a certificate truststore must be used.

### Using Custom Certificates
- Custom certificates for the Alert web server to present to clients.

    - Before custom certificates can be used for Alert the signed certificate and key must be available.

        - WEBSERVER_CUSTOM_CERT_FILE - The file containing the customer's signed certificate.
            ```bash
            cp <PATH_TO_CERT_FILE> <PATH_TO_SECRETS>/WEBSERVER_CUSTOM_CERT_FILE
            ```
            - Replace <PATH_TO_CERT_FILE> with the path to the certificate file.
            - Replace <PATH_TO_SECRETS> with the directory path where the secrets files are stored.  See step 1 of the installation being performed. 

        - WEBSERVER_CUSTOM_KEY_FILE - The file containing the customer's key used to create the certificate.
            ```bash
            cp <PATH_TO_KEY_FILE> <PATH_TO_SECRETS>/WEBSERVER_CUSTOM_KEY_FILE
            ```
            - Replace <PATH_TO_KEY_FILE> with the path to the certificate file.
            - Replace <PATH_TO_SECRETS> with the directory path where the secrets files are stored.  See step 1 of the installation being performed.

### Using Custom Certificate TrustStore
- Custom java TrustStore file for the Alert server to communicate over SSL to external systems.

    Must have a valid JKS trust store file that can be used as the TrustStore for Alert.  
    If certificate errors arise, then this is the TrustStore where certificates will need to be imported to resolve those issues.

    Only one of the following secrets needs to be created.  If both are created, then jssecacerts secret will take precedence and be used by Alert.

    - Create the secret.  Only create one of the following secrets.
        - jssecacerts - The java TrustStore file with any custom certificates imported.
            ```bash
            cp <PATH_TO_TRUST_STORE_FILE> <PATH_TO_SECRETS>/jssecacerts
            ```
        or

        - cacerts - The java TrustStore file with any custom certificates imported.
            ```bash
            cp <PATH_TO_TRUST_STORE_FILE> <PATH_TO_SECRETS>/cacerts
            ```
    - Replace <PATH_TO_TRUST_STORE_FILE> with the path to the TrustStore file to be used.
    - Replace <PATH_TO_SECRETS> with the directory path where the secrets files are stored.  See step 1 of the installation being performed.
    - Find and uncomment the ALERT_TRUST_STORE_PASSWORD environment variable from the docker-compose.local-overrides.yml file.
    
    Example:
    ```yaml
    alert:
      environment:
        - ALERT_TRUST_STORE_PASSWORD=<PASSWORD>
    ```
    - Replace <PASSWORD> with the password for the jssecacerts or cacerts that was copied as a secret above.
    
### Insecure Trust of All Certificates
WARNING: This is not a recommended option. Using this option makes your deployment less secure. Use at your own risk.
Certificates SHOULD be correctly generated for the Alert server and a valid TrustStore SHOULD be provided to trust third party systems.

This option allows the bypass of all certificate verification in the event that external certificates can not be imported into the Alert TrustStore, or certificate errors continue after importing the external certificates into the Alert TrustStore.

To allow Alert to trust all certificates add the environment variable:
```bash
ALERT_TRUST_CERT=true
```
Please see the section [Environment Variables](#environment-variables) to learn how to set the environment variables.


## Environment Variables
Alert supports configuration of the application's components via environment variables.
Edit the ```docker-compose.local-overrides.yml``` file to include the environment variables.

### Editing the Overrides File
- Verify that ```alert:``` is uncommented from the service section, otherwise uncomment the ```alert:``` of docker-compose.local-overrides.yml.
- Uncomment ```environment:``` from the alert service section of docker-compose.local-overrides.yml. 
- Environment variables have the format ```- <VARIABLE_NAME>=<VARIABLE_VALUE>``` 
- Environment variables are commented out in the ```docker-compose.local-overrides.yml``` file. 
- Uncomment the environment variables to be used from the ```environment: ``` section of the alert service.

Example:
```yaml
alert:
    environment:
        - ALERT_HOSTNAME=localhost
```

### Environment Variable Overrides
The environment variables will always take precedence and overwrite the values stored in the database if the following variable value is set to 'true'.
```bash
ALERT_COMPONENT_SETTINGS_SETTINGS_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE=true
```

Please note the following with the override environment variable set. 
If any other environment variable is set with no value or an empty string, then the corresponding value (if any) in the database will be removed.

### Alert Hostname Variable
The ALERT_HOSTNAME environment variable must be specified in order for Alert to generate and use certificates correctly.
- Add the ALERT_HOSTNAME environment variable. (The value must be the hostname only.)
    - Editing overrides file:
    ```yaml
    alert:
        environment:
            - ALERT_HOSTNAME=<NEW_HOST_NAME>
    ```
    - Replace <NEW_HOST_NAME> with the hostname of the machine where Alert is installed.
- Do not add the protocol a.k.a scheme to the value of the variable.
    - Good: ```ALERT_HOSTNAME=myhost.example.com```
    - Bad: ```ALERT_HOSTNAME=https://myhost.example.com```

### Alert Logging Level Variable
To change the logging level of Alert add the following environment variable to the deployment.

- Editing overrides file:
    ```yaml
    alert:
        environment:
            - ALERT_LOGGING_LEVEL=DEBUG
    ```

- Set the value to one of the following:
    - DEBUG
    - ERROR
    - INFO
    - TRACE
    - WARN

### Black Duck Web Server Host
The PUBLIC_HUB_WEBSERVER_HOST environment variable should be specified when you are installing Alert with Black Duck and the Black Duck instance. 
If a PKIX error occurs when configuring the Black Duck provider in Alert, then specifying this environment variable may solve the problem.
Alert will attempt to import the Black Duck server's certificate into the Trust Store Alert uses. 

- Add PUBLIC_HUB_WEBSERVER_HOST environment variable. (The value must be the hostname only.)

    - Editing overrides file:
    ```yaml
    alert:
        environment:
            - PUBLIC_HUB_WEBSERVER_HOST=<BLACK_DUCK_HOST_NAME>
    ```
    - Replace <BLACK_DUCK_HOST_NAME> with the hostname of the machine where Black Duck is installed.
- Do not add the protocol a.k.a scheme to the value of the variable.
    - Good: ```PUBLIC_HUB_WEBSERVER_HOST=blackduck.example.com```
    - Bad: ```PUBLIC_HUB_WEBSERVER_HOST=https://blackduck.example.com```   
    
### Black Duck Web Server Port
The PUBLIC_HUB_WEBSERVER_PORT environment variable should be specified if Black Duck is running on another port other than the default https (443) port.

- Add PUBLIC_HUB_WEBSERVER_HOST environment variable. (The value must be the hostname only.)

    - Editing overrides file:
    ```yaml
    alert:
        environment:
            - PUBLIC_HUB_WEBSERVER_PORT=<BLACK_DUCK_PORT>
    ```
    - Replace <BLACK_DUCK_PORT> with the hostname of the machine where Black Duck is installed. 

### Email Channel Environment Variables
A majority of the Email Channel environment variables that can be set are related to JavaMail configuration properties. The JavaMail properties can be found here:

[JavaMail Properties](https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html)

- The Email Channel environment variables have a prefix of ```ALERT_CHANNEL_EMAIL_```
- The remaining portion of the variable, after the prefix, map to the JavaMail properties if the '_' character is replaced with '.'

Examples:
- ALERT_CHANNEL_EMAIL_MAIL_SMTP_HOST maps to 'mail.smtp.host'
- ALERT_CHANNEL_EMAIL_MAIL_SMTP_PORT maps to 'mail.smtp.port'

### Environment Variable Classifications
There are certain classifications with the environment variables expressed by a specific naming convention:
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
If Alert should not be running on its default port of 8443, then this section describes what must be changed in order to use a different port.

For this advanced setting, since there are more than just environment variables that need to be set, edit the ```docker-compose.local-overrides.yml``` file.

- Overrides File Changes.
    - Define the new ports for the alert service.  Add 'ports' to the service description.
    ```yaml
        alert:
            ports: ['<NEW_PORT>:<NEW_PORT>']
    ```
    - Define the ```ALERT_SERVER_PORT``` environment variable.
    ```yaml
        alert:
            environment:
                - ALERT_HOSTNAME=localhost
                - ALERT_SERVER_PORT=<NEW_PORT>
    ```
    - Define the healthcheck for the alert service. Add 'healthcheck' to the service description.
    ```yaml
        alert:
            healthcheck:
                  test: [CMD, /usr/local/bin/docker-healthcheck.sh, 'https://localhost:<NEW_PORT>/alert/api/about',
                         /opt/blackduck/alert/security/root.crt, /opt/blackduck/alert/security/blackduck_system.crt,
                         /opt/blackduck/alert/security/blackduck_system.key]
                  interval: 30s
                  timeout: 60s
                  retries: 15
    ```
    - Replace <NEW_PORT> with the port to be used.
    
Example:
- Change the port to 9090 via the ```docker-compose.local-overrides.yml``` file.
```yaml
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
If Alert should be using more memory than its default settings, then this section describes what must be changed in order to allocate more memory.

For this advanced setting, since there are more than just environment variables that need to be set, edit the ```docker-compose.local-overrides.yml``` file.

- Overrides File Changes.
    - Define the ```ALERT_MAX_HEAP_SIZE``` environment variable:
    ```yaml
        alert:
            environment:
                - ALERT_HOSTNAME=localhost
                - ALERT_MAX_HEAP_SIZE=<NEW_HEAP_SIZE>
    ```
    - Define the container memory limit. Add 'mem_limit' to the service description.
    ```yaml
        alert:
            mem_limit: <NEW_HEAP_SIZE + 256M>
    ```
    - Replace <NEW_HEAP_SIZE> with the heap size to be used.
    Note:
        The ALERT_MAX_HEAP_SIZE and the container mem_limit settings should not be exactly the same.  
        The container mem_limit setting is the maximum memory allocated to the container.  
        Additional memory does not get allocated to it.  
        The maximum heap size in Java is the maximum size of the heap in the Java virtual machine (JVM), but the JVM also uses additional memory.  
        Therefore, the ALERT_MAX_HEAP_SIZE environment variable must be less than the amount defined in the mem_limit which is set for the container.
        Synopsys recommends setting the mem_limit using the following formula: ALERT_MAX_HEAP_SIZE + 256M.

            ALERT_MAX_HEAP_SIZE = 4096M
            mem_limit = ALERT_MAX_HEAP_SIZE + 256M = 4352M

Example:
- Change the memory limit from 2G to 4G.
```yaml
    alert:
        environment:
            - ALERT_HOSTNAME=localhost
            - ALERT_MAX_HEAP_SIZE=4096M
        mem_limit: 4352M
```

Note: Work with your IT staff if necessary to verify the configured memory is available on the host machine.
