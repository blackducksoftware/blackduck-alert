# Black Duck Alert On Docker Swarm

This document describes how to install and upgrade Alert in Docker Swarm.

## Table Of Contents
- [Requirements](#requirements)
- [Installing Alert](#installing-alert)
  - [Installation](#installation)
- [Upgrading Alert](#upgrading-alert)
  - [Verify Secrets](#verify-secrets)
  - [Upgrade](#upgrade)
- [Certificates](#certificates)
  - [Using Custom Certificates](#using-custom-certificates)
  - [Using Custom Certificate Truststore](#using-custom-certificate-truststore)
- [Environment Variables](#environment-variables)
  - [Editing the Overrides File](#editing-the-overrides-file)
  - [Alert Hostname Variable](#alert-hostname-variable)
  - [Alert Database Variables](#alert-database-variables)
  - [Alert Logging Level Variable](#alert-logging-level-variable)
  - [Email Channel Environment Variables](#email-channel-environment-variables)
  - [Environment Variable Classifications](#environment-variable-classifications)
- [Advanced Configuration](#advanced-configuration)
  - [Changing Server Port](#changing-server-port)
  - [Changing Memory Settings](#changing-memory-settings)

## Requirements
- A Docker host with at least 2GB of allocatable memory.
- Administrative access to the docker host machine. 
- You must create a Postgres user and database that the created user owns for Alert to create the database structure.
- Before installing or upgrading Alert the desired persistent storage volumes must be created for Alert and needs to be either:
    - Node locked.     
    - Backed by an NFS volume or a similar mechanism.
- Postgres Version: 12
- Extension: uuid-ossp (Note: this should be installed prior to creating the database)
- Schemas: public, alert
- Roles/Privileges: Alert requires two sets of Postgres Privileges. One set of privileges is necessary for initializing and upgrading the database. The other set (which is a subset of the first) is for reading and writing data when the
  application is running. Although it is possible to use one role to handle both of these cases, it is recommended to separate them.
    - Ensure the DB roles have the public schema on their search_path(s):
      ```bash
      ALTER ROLE <user> SET search_path = "$user", public;
      ```
    - The Alert Admin role should have the following privileges on all objects in the alert schema (or the schema/database when relevant):
        - SELECT
        - INSERT
        - CREATE
        - UPDATE
        - DELETE
        - TRUNCATE
        - REFERENCES
        - TRIGGER
        - TEMPORARY
        - EXECUTE
        - USAGE
    - The Alert Admin role should have the following privileges on all objects in the public schema (or the schema/database when relevant):
        - SELECT
        - INSERT
        - UPDATE
        - DELETE
        - EXECUTE
    - The Alert User role should have the following privileges on all objects in the alert schema (or the schema/database when relevant):
        - SELECT
        - INSERT
        - UPDATE
        - DELETE
        - EXECUTE

## Installing Alert

Deployment files for Docker Swarm are located in the *docker-swarm* directory of the zip file.

```text
blackduck-alert-<VERSION>-deployment.zip file.
```

- Extract the contents of the ZIP file.
- Choose your installation type standalone or with Black Duck.
- For installing with Black Duck the files are located in the *hub* sub-directory.
- For installing Alert standalone the files are located in the *standalone* sub-directory.

** Please Note **
This document covers editing only the `docker-compose.local-overrides.yml` file that is contained in the *docker-swarm* directory.

### Installation

This section will walk through the instructions to install Alert in a standalone fashion.

#### Overview

1. Create ALERT_ENCRYPTION_PASSWORD secret.
2. Create ALERT_ENCRYPTION_GLOBAL_SALT secret.
3. Create ALERT_DB_USERNAME secret.
4. Create ALERT_DB_PASSWORD secret.
5. Create ALERT_DB_ADMIN_USERNAME secret.
6. Create ALERT_DB_ADMIN_PASSWORD secret.
7. Manage certificates.
8. Modify environment variables.
9. Deploy the stack.

**Please Note:**
If you are installing with Black Duck and upgrading Alert from a 4.x version to 5.x, please use the docker-compose.local-overrides.yml bundled with Alert. Please remove any Alert configuration from the docker-compose.local-overrides.yml
file bundled with Black Duck.

#### Details

This section will walk through each step of the installation procedure.

##### 1. Create ALERT_ENCRYPTION_PASSWORD secret.

- Create a docker secret containing the encryption password for Alert.
    ```bash
    docker secret create <STACK_NAME>_ALERT_ENCRYPTION_PASSWORD <FILE_CONTAINING_PASSWORD>
    ```
  - Replace <STACK_NAME> with the name of the stack to be used in the deployment.
    - Replace <FILE_CONTAINING_PASSWORD> with the path to the file containing the password text.
    
- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file.
- Uncomment the following from the docker-compose.local-overrides.yml file alert service section.
    ```yaml
        alert:
            secrets:
                - ALERT_ENCRYPTION_PASSWORD
    ```
    
- Uncomment the following from the secrets section of the docker-compose.local-overrides.yml file.
    ```yaml
        secrets:
            ALERT_ENCRYPTION_PASSWORD:
              external: true
              name: "<STACK_NAME>_ALERT_ENCRYPTION_PASSWORD"
    ```
    - Replace <STACK_NAME> with the name of the stack to be used in the deployment.

##### 2. Create ALERT_ENCRYPTION_GLOBAL_SALT secret.

- Create a docker secret containing the encryption salt for Alert.
    ```bash
    docker secret create <STACK_NAME>_ALERT_ENCRYPTION_GLOBAL_SALT <FILE_CONTAINING_SALT>
    ```
    - Replace <STACK_NAME> with the name of the stack to be used in the deployment.
    - Replace <FILE_CONTAINING_SALT> with the path to the file containing the salt text.
    
    Note: If you created the secret ALERT_ENCRYPTION_SALT in a version of Alert prior to 5.x, please use the same salt value for the ALERT_ENCRYPTION_GLOBAL_SALT secret.
    
- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file.
- Uncomment the following from the docker-compose.local-overrides.yml file alert service section.
    ```yaml
        alert:
            secrets:
                - ALERT_ENCRYPTION_PASSWORD
                - ALERT_ENCRYPTION_GLOBAL_SALT
    ```
- Uncomment the following from the secrets section of the docker-compose.local-overrides.yml file.
    ```yaml
        secrets:
            ALERT_ENCRYPTION_PASSWORD:
              external: true
              name: "<STACK_NAME>_ALERT_ENCRYPTION_PASSWORD"
            ALERT_ENCRYPTION_GLOBAL_SALT:
              external: true
              name: "<STACK_NAME>_ALERT_ENCRYPTION_GLOBAL_SALT"
    ```
    - Replace <STACK_NAME> with the name of the stack to be used in the deployment.

##### 3. Create ALERT_DB_USERNAME secret.

- Create a docker secret containing the database username for Alert.
    ```bash
    docker secret create <STACK_NAME>_ALERT_DB_USERNAME <FILE_CONTAINING_USER_NAME>
    ```
    - Replace <STACK_NAME> with the name of the stack to be used in the deployment.
    - Replace <FILE_CONTAINING_USER_NAME> with the path to the file containing the database username.
    
- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file.
- Uncomment the following secrets from the docker-compose.local-overrides.yml file alert service section.
    ```yaml
        alert:
            secrets:
              - ALERT_DB_USERNAME
    ```
- Uncomment the following from the secrets section of the docker-compose.local-overrides.yml file.
    ```yaml
        secrets:
          ALERT_DB_USERNAME:
            external: true
            name: "<STACK_NAME>_ALERT_DB_USERNAME"
    ```
    - Replace <STACK_NAME> with the name of the stack to be used in the deployment.

##### 4. Create ALERT_DB_PASSWORD secret.

- Create a docker secret containing the database password for Alert.
    ```bash
    docker secret create <STACK_NAME>_ALERT_DB_PASSWORD <FILE_CONTAINING_PASSWORD>
    ```
    - Replace <STACK_NAME> with the name of the stack to be used in the deployment.
    - Replace <FILE_CONTAINING_PASSWORD> with the path to the file containing the database password.
    
- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file.
- Uncomment the following secrets from the docker-compose.local-overrides.yml file alert service section.
    ```yaml
        alert:
            secrets:
              - ALERT_DB_PASSWORD
    ```
- Uncomment the following from the secrets section of the docker-compose.local-overrides.yml file.
    ```yaml
        secrets:
          ALERT_DB_PASSWORD:
            external: true
            name: "<STACK_NAME>_ALERT_DB_PASSWORD"
    ```
    - Replace <STACK_NAME> with the name of the stack to be used in the deployment.

##### 5. Create ALERT_DB_ADMIN_USERNAME secret.

- Create a docker secret containing the database admin username for Alert.
    ```bash
    docker secret create <STACK_NAME>_ALERT_DB_ADMIN_USERNAME <FILE_CONTAINING_ADMIN_USER_NAME>
    ```
    - Replace <STACK_NAME> with the name of the stack to be used in the deployment.
    - Replace <FILE_CONTAINING_USER_NAME> with the path to the file containing the database admin username.
    
- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file.
- Uncomment the following secrets from the docker-compose.local-overrides.yml file alert service section.
    ```yaml
        alert:
            secrets:
              - ALERT_DB_ADMIN_USERNAME
    ```
- Uncomment the following from the secrets section of the docker-compose.local-overrides.yml file.
    ```yaml
        secrets:
          ALERT_DB_ADMIN_USERNAME:
            external: true
            name: "<STACK_NAME>_ALERT_DB_ADMIN_USERNAME"
    ```
    - Replace <STACK_NAME> with the name of the stack to be used in the deployment.
    
##### 6. Create ALERT_DB_ADMIN_PASSWORD secret.

- Create a docker secret containing the database admin password for Alert.
    ```bash
    docker secret create <STACK_NAME>_ALERT_DB_ADMIN_PASSWORD <FILE_CONTAINING_ADMIN_PASSWORD>
    ```
    - Replace <STACK_NAME> with the name of the stack to be used in the deployment.
    - Replace <FILE_CONTAINING_ADMIN_PASSWORD> with the path to the file containing the database admin password.
    
- Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file.
- Uncomment the following secrets from the docker-compose.local-overrides.yml file alert service section.
    ```yaml
        alert:
            secrets:
              - ALERT_DB_ADMIN_PASSWORD
    ```
- Uncomment the following from the secrets section of the docker-compose.local-overrides.yml file.
    ```yaml
        secrets:
          ALERT_DB_ADMIN_PASSWORD:
            external: true
            name: "<STACK_NAME>_ALERT_DB_ADMIN_PASSWORD"
    ```
  - Replace <STACK_NAME> with the name of the stack to be used in the deployment.

##### 7. Manage certificates.

This is an optional step. Confirm if custom certificates or certificate store need to be used.

- Using custom certificate for Alert web server. See [Using Custom Certificates](#using-custom-certificates)
- Using custom trust store to trust certificates of external servers. See [Using Custom Certificate TrustStore](#using-custom-certificate-truststore)

#### 8. Modify environment variables.

Please see [Environment Variables](#environment-variables)

- Set the required environment variable ALERT_HOSTNAME. See [Alert Hostname Variable](#alert-hostname-variable)
- Set the required environment variables for database connectivity. See [Alert Database Variables](#alert-database-variables)
- Set any other optional environment variables as needed.

##### 9. Deploy the stack.

This step has some variation in how a user accesses Alert depending on the installation you are trying to perform.  
There are the Standalone and Installing with Black Duck variations.

###### Standalone

When deploying this way you intend to access Alert through the configured public hostname and port for the Alert service. The public hostname and/or port configured for Alert is different from the public hostname and/or port of the Black
Duck installation.

Alert will be accessible via https://<ALERT_HOSTNAME>:<ALERT_SERVER_PORT>/alert

- Execute the command:
    ```bash
    docker stack deploy -c <PATH>/docker-swarm/external-db/standalone/docker-compose.yml -c <PATH>/docker-swarm/docker-compose.local-overrides.yml <STACK_NAME>
    ```
  - Replace <STACK_NAME> with the name of the stack to be used in the deployment.
  - Replace <PATH> with the directory path to the Alert installation files.

###### Installation with Black Duck

When deploying this way you intend to access Alert through the same public hostname and port as Black Duck. Black Duck is used as a reverse proxy to the Alert service. This is different from a standalone installation.

Alert will be accessible via https://<BLACK_DUCK_HOST_NAME>:<BLACK_DUCK_PORT>/alert

- First verify that Black Duck is installed properly with the correct configuration to support Alert in this way.
  - This is covered in the Black Duck installation documentation.
  - Follow the installation procedure for installing Black Duck if Black Duck is not already installed to support Alert.

Note: The NGinX container will not start correctly when it is waiting for the alert service to be available.  
Deploy Alert onto the stack and NGinX will eventually become healthy when the alert service is up and running.

- Execute the command to add Alert to the stack:
    ```bash
    docker stack deploy -c <PATH>/docker-swarm/external-db/hub/docker-compose.yml -c <PATH>/docker-swarm/docker-compose.local-overrides.yml <STACK_NAME>
    ```
  - Replace <STACK_NAME> with the name of the stack to be used in the deployment.
  - Replace <PATH> with the directory path to the Alert installation files.
  - Use the same stack name used to install Black Duck i.e. blackduck.
    ```bash
    docker stack deploy -c <PATH>/docker-swarm/external-db/hub/docker-compose.yml -c <PATH>/docker-swarm/docker-compose.local-overrides.yml blackduck
    ```

## Upgrading Alert

Remove the stack and then re-deploy the stack. The steps in the upgrade procedure are the same as the installation procedure after removing the stack.

### Verify Secrets

1. Review the docker secrets.
    ```bash
    docker secret ls
    ```

### Upgrade

1. Run `docker stack rm <STACK_NAME>` replacing <STACK_NAME> with the name of the stack to be used in the deployment.
2. Follow the [Installation](#installation) instructions.

## Certificates

This section describes how to configure the optional certificates. Please verify beforehand if custom certificates or certificate truststore must be used.

### Using Custom Certificates

- Custom certificates for the Alert Web server to present to clients.

  - Before custom certificates can be used for Alert the signed certificate and key must be available.

    - WEBSERVER_CUSTOM_CERT_FILE - The file containing the customer's signed certificate.
      ```bash
      docker secret create <STACK_NAME>_WEBSERVER_CUSTOM_CERT_FILE <PATH_TO_CERT_FILE>	
      ```
      - Replace <STACK_NAME> with the name of the stack to be used in the deployment.
      - Replace <PATH_TO_CERT_FILE> with the path to the certificate file.

      - WEBSERVER_CUSTOM_KEY_FILE - The file containing the customer's key used to create the certificate.

        ```bash
        docker secret create <STACK_NAME>_WEBSERVER_CUSTOM_KEY_FILE <PATH_TO_KEY_FILE>	
        ```
      - Replace <STACK_NAME> with the name of the stack to be used in the deployment.
      - Replace <PATH_TO_KEY_FILE> with the path to the certificate file.

    - Uncomment the following secrets from the docker-compose.local-overrides.yml file alert service section.
    ```yaml
        alert:	
            secrets:	
                - WEBSERVER_CUSTOM_CERT_FILE	
                - WEBSERVER_CUSTOM_KEY_FILE	
    ```
    - Uncomment the following secrets from the secrets section of the docker-compose.local-overrides.yml file.	
    ```yaml
        secrets:	
            WEBSERVER_CUSTOM_CERT_FILE:	
                external: true	
                name: "<STACK_NAME>_WEBSERVER_CUSTOM_CERT_FILE"	
            WEBSERVER_CUSTOM_KEY_FILE:	
                external: true	
                name: "<STACK_NAME>_WEBSERVER_CUSTOM_KEY_FILE"	
    ```
    - Replace <STACK_NAME> with the name of the stack to be used in the deployment.	

### Using Custom Certificate TrustStore	
- Custom java TrustStore file for the Alert server to communicate over SSL to external systems.	

    You can import certificates via the Alert UI if you log in as a system administrator.  This is the preferred option.
    You may follow these instructions to supply a TrustStore on application startup.

    Must have a valid JKS trust store file that can be used as the TrustStore for Alert.  	
    If certificate errors arise, then this is the TrustStore where certificates will need to be imported to resolve those issues. 	

    Only one of the following secrets needs to be created.  If both are created, then jssecacerts secret will take precedence and be used by Alert.	

    - Create the secret.  Only create one of the following secrets.	
        - jssecacerts - The java TrustStore file with any custom certificates imported.	
            ```bash
            docker secret create <STACK_NAME>_jssecacerts <PATH_TO_TRUST_STORE_FILE>	
            ```
            - Replace <STACK_NAME> with the name of the stack to be used in the deployment.	
            - Replace <PATH_TO_TRUST_STORE_FILE> with the path to the TrustStore file to be used.	

        or 	

        - cacerts - The java TrustStore file with any custom certificates imported. 	
            ```bash
            docker secret create <STACK_NAME>_cacerts <PATH_TO_TRUST_STORE_FILE>	
            ```
            - Replace <STACK_NAME> with the name of the stack to be used in the deployment.	
            - Replace <PATH_TO_TRUST_STORE_FILE> with the path to the TrustStore file to be used.	

    - Uncomment the following from the docker-compose.local-overrides.yml file from the secrets section near the bottom of the file.	
    ```yaml
        secrets:	
            jssecacerts:	
                external: true	
                name: "<STACK_NAME>_jssecacerts"	
    ```
    or 	
    ```yaml
        secrets:	
            cacerts:	
                external: true	
                name: "<STACK_NAME>_cacerts"	
    ```
    - Replace <STACK_NAME> with the name of the stack to be used in the deployment.	
    - Uncomment the following from the docker-compose.local-overrides.yml file from the services alert section	
    ```yaml
        secrets:	
            - source: jssecacerts	
              target: jssecacerts	
              mode: 0664	
    ```
    or	
    ```yaml
        secrets:	
            - source: cacerts	
              target: cacerts	
              mode: 0664	
    ```
    Note: The mode (file permissions) must be specified because the certificate file is copied to a location Alert uses internally. Read/Write permissions are required to copy the file and import certificates into the TrustStore.	

    - Create a docker secret containing the password for the trust store.	
        ```bash
        docker secret create <STACK_NAME>_ALERT_TRUST_STORE_PASSWORD <FILE_CONTAINING_PASSWORD>	
        ```
        - Replace <STACK_NAME> with the name of the stack to be used in the deployment.	
        - Replace <FILE_CONTAINING_PASSWORD> with the path to the file containing the password text.	

    - Make sure the alert service is uncommented from the docker-compose.local-overrides.yml file.	
    - Uncomment the following from the docker-compose.local-overrides.yml file alert service section.	
        ```yaml
            alert:	
                secrets:	
                    - ALERT_TRUST_STORE_PASSWORD	
        ```
    - Uncomment the following from the secrets section of the docker-compose.local-overrides.yml file.	
        ```yaml
            secrets:	
                ALERT_TRUST_STORE_PASSWORD:	
                  external: true	
                  name: "<STACK_NAME>_ALERT_TRUST_STORE_PASSWORD"
        ```
        - Replace <STACK_NAME> with the name of the stack to be used in the deployment.	

## Environment Variables
Alert supports initial configuration of the application's components via environment variables.  
Environment variable values are only used if there is no configuration data for the corresponding component in the database.
Please see the [Environment Variable Classifications](#environment-variable-classifications) to understand how environment variables pertain to Alert configuration data.
Edit the `docker-compose.local-overrides.yml` file to include the environment variables.

### Editing the Overrides File
- Verify that `alert:` is uncommented from the service section, otherwise uncomment the `alert:` of docker-compose.local-overrides.yml.
- Uncomment `environment:` from the alert service section of docker-compose.local-overrides.yml. 
- Environment variables have the format `- <VARIABLE_NAME>=<VARIABLE_VALUE>` 
- Environment variables are commented out in the `docker-compose.local-overrides.yml` file. 
- Uncomment the environment variables to be used from the `environment: ` section of the alert service.

Example:
```yaml
alert:
    environment:
        - ALERT_HOSTNAME=localhost
```

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
    - Good: `ALERT_HOSTNAME=myhost.example.com`
    - Bad: `ALERT_HOSTNAME=https://myhost.example.com`

### Alert Database Variables
There are additional environment variables to control how Alert connects to a database independent of the user and password secrets.
These include ALERT_DB_HOST, ALERT_DB_PORT, and ALERT_DB_NAME.
They are defined in the overrides file under the comment: 
    ```yaml
    # -- Database Settings
    ```
- Add the ALERT_DB_HOST environment variable (The value must be the hostname only of the database.)
    - Editing overrides file:
        ```yaml
        alert:
            environment:
                - ALERT_DB_HOST=<DB_HOST_NAME>
        ```
    - Replace <DB_HOST_NAME> with the hostname of the machine where Postgres is installed.
    - Do not add the protocol a.k.a scheme to the value of the variable.
        - Good: `ALERT_DB_HOST=myhost.example.com`
        - Bad: `ALERT_DB_HOST=https://myhost.example.com`

- Add the ALERT_DB_PORT environment variable
    - Editing overrides file:
        ```yaml
        alert:
            environment:
                - ALERT_DB_PORT=<DB_PORT>
        ```
    - Replace <DB_PORT> with the port used by the Postgres database (default is 5432.)
- Add the ALERT_DB_NAME environment variable
    - Editing overrides file:
        ```yaml
        alert:
            environment:
                - ALERT_DB_NAME=<DB_NAME>
        ```
    - Replace <DB_NAME> with the name of the database created in Postgres to store Alert data.
            
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

### Email Channel Environment Variables
A majority of the Email Channel environment variables that can be set are related to JavaMail configuration properties. The JavaMail properties can be found here: 

[JavaMail Properties](https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html)

- The Email Channel environment variables have a prefix of `ALERT_CHANNEL_EMAIL_`
- The remaining portion of the variable, after the prefix, map to the JavaMail properties if the '_' character is replaced with '.'

Examples:
- ALERT_CHANNEL_EMAIL_MAIL_SMTP_HOST maps to 'mail.smtp.host'
- ALERT_CHANNEL_EMAIL_MAIL_SMTP_PORT maps to 'mail.smtp.port'

### Environment Variable Classifications
There are certain classifications with the environment variables expressed by a specific naming convention:
`ALERT_<CLASSIFICATION>_<ITEM_NAME>_<CONFIGURATION_PROPERTY>`
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
    - ALERT_COMPONENT_AUTHENTICATION_SETTINGS_LDAP_ENABLED= Boolean to determine if LDAP authentication is used.

## Advanced Configuration
This section describes some advanced configuration settings for the Alert server.

### Changing Server Port
If Alert should not be running on its default port of 8443, then this section describes what must be changed in order to use a different port.

For this advanced setting, since there are more than just environment variables that need to be set, edit the `docker-compose.local-overrides.yml` file.

- Overrides File Changes
    - Define the new ports for the alert service.  Add 'ports' to the service description. 
    ```yaml
        alert: 
            ports: ['<NEW_PORT>:<NEW_PORT>']
    ```
    - Define the `ALERT_SERVER_PORT` environment variable.
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
- Change the port to 9090 via the `docker-compose.local-overrides.yml` file for the blackduck stack.
```yaml
    alert:
        ports: ['9090:9090']
        environment:
            - ALERT_HOSTNAME=localhost
            - ALERT_SERVER_PORT=9090
        secrets:
            - ALERT_ENCRYPTION_PASSWORD
            - ALERT_ENCRYPTION_GLOBAL_SALT
        healthcheck:
            test: [CMD, /usr/local/bin/docker-healthcheck.sh, 'https://localhost:9090/alert/api/about',
                 /opt/blackduck/alert/security/root.crt, /opt/blackduck/alert/security/blackduck_system.crt,
                /opt/blackduck/alert/security/blackduck_system.key]
            interval: 30s
            timeout: 60s
            retries: 15
    secrets:
        ALERT_ENCRYPTION_PASSWORD:
            external: true
            name: "blackduck_ALERT_ENCRYPTION_PASSWORD"
        ALERT_ENCRYPTION_GLOBAL_SALT:
            external: true
            name: "blackduck_ALERT_ENCRYPTION_GLOBAL_SALT"
```
  
Note: Work with your IT staff if necessary to verify the configured port is accessible through the network.

### Changing Memory Settings
If Alert should be using more memory than its default settings, then this section describes what must be changed in order to allocate more memory.

For this advanced setting, since there are more than just environment variables that need to be set, edit the ```docker-compose.local-overrides.yml``` file.

- Overrides File Changes.
    - Define the `ALERT_MAX_HEAP_SIZE` environment variable:
    ```yaml
        alert:
            environment:
                - ALERT_HOSTNAME=localhost
                - ALERT_MAX_HEAP_SIZE=<NEW_HEAP_SIZE>
    ```
    - Define the container memory limit. Add the deploy section to the alert service description.
    ```yaml
        alert:
            deploy:
                resources:
                    limits: {memory: <NEW_HEAP_SIZE + 256M>}
                    reservations: {memory: <NEW_HEAP_SIZE + 256M>}: 
    ```
    - Replace <NEW_HEAP_SIZE> with the heap size to be used.
    Note: 
        The ALERT_MAX_HEAP_SIZE and the container deploy.resources settings should not be exactly the same.  
        The container deploy.resources setting is the maximum memory allocated to the container.  
        Additional memory does not get allocated to it.  
        The maximum heap size in Java is the maximum size of the heap in the Java virtual machine (JVM), but the JVM also uses additional memory.  
        Therefore, the ALERT_MAX_HEAP_SIZE environment variable must be less than the amount defined in the mem_limit which is set for the container. 
        Synopsys recommends setting the deploy.resources using the following formula: ALERT_MAX_HEAP_SIZE + 256M.
        ```bash
            ALERT_MAX_HEAP_SIZE = 4096M
            limits = ALERT_MAX_HEAP_SIZE + 256M = 4352M
            reservations = ALERT_MAX_HEAP_SIZE + 256M = 4352M
        ```
                
Example: 
- Change the memory limit from 2G to 4G.
```yaml
    alert:
        environment:
            - ALERT_HOSTNAME=localhost
            - ALERT_MAX_HEAP_SIZE=4096M
        deploy:
            resources:
                limits: {memory: 4352M}
                reservations: {memory: 4352M}
```

Note: Work with your IT staff if necessary to verify the configured memory is available on the host machine.
