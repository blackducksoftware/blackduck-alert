#!/bin/sh

## ALERT VARIABLES ##
alertDatabaseDir="${ALERT_DATA_DIR}/alertdb"
upgradeResourcesDir="${ALERT_TAR_HOME}/upgradeResources"
alertDatabaseHost="${ALERT_DB_HOST:-alertdb}"
alertDatabasePort="${ALERT_DB_PORT:-5432}"
alertDatabaseName="${ALERT_DB_NAME:-alertdb}"
alertDatabaseUser="${ALERT_DB_USERNAME:-sa}"
alertDatabasePassword="${ALERT_DB_PASSWORD:-blackduck}"
alertDatabaseAdminUser="${ALERT_DB_ADMIN_USERNAME:-$alertDatabaseUser}"
alertDatabaseAdminPassword="${ALERT_DB_ADMIN_PASSWORD:-$alertDatabasePassword}"
alertDatabaseSslMode="${ALERT_DB_SSL_MODE:-allow}"
alertDatabaseSslKey=${ALERT_DB_SSL_KEY}
alertDatabaseSslCert=${ALERT_DB_SSL_CERT}
alertDatabaseSslRootCert=${ALERT_DB_SSL_ROOT_CERT}
alertHostName="${ALERT_HOSTNAME:-localhost}"

## CERTIFICATE VARIABLES ##
serverCertName=$APPLICATION_NAME-server
dockerSecretDir=${RUN_SECRETS_DIR:-/run/secrets}
keyStoreFile=$APPLICATION_NAME.keystore
keystoreFilePath=${SECURITY_DIR}/$keyStoreFile
keystorePassword="${ALERT_KEY_STORE_PASSWORD:-changeit}"
truststoreFile=${SECURITY_DIR}/$APPLICATION_NAME.truststore
truststorePassword="${ALERT_TRUST_STORE_PASSWORD:-changeit}"

## OTHER VARIABLES ##
targetCAHost="${HUB_CFSSL_HOST:-cfssl}"
targetCAPort="${HUB_CFSSL_PORT:-8888}"
targetWebAppHost="${HUB_WEBAPP_HOST:-alert}"

[ -z "$ALERT_HOSTNAME" ] && echo "Alert Host: [$alertHostName]. Wrong host name? Restart the container with the right host name configured in blackduck-alert.env"

if [ -e $dockerSecretDir/ALERT_TRUST_STORE_PASSWORD ];
then
  echo "Trust Store secret set; using value from secret."
  truststorePassword=$(cat $dockerSecretDir/ALERT_TRUST_STORE_PASSWORD | xargs echo)
fi

if [ -e $dockerSecretDir/ALERT_KEY_STORE_PASSWORD ];
then
  echo "Key Store secret set; using value from secret."
  keystorePassword=$(cat $dockerSecretDir/ALERT_KEY_STORE_PASSWORD | xargs echo)
fi

if [ -e $dockerSecretDir/ALERT_RABBIT_USER ];
then
  echo "RabbitMQ user secret set; using value from secret."
  export ALERT_RABBIT_USER=$(cat $dockerSecretDir/ALERT_RABBIT_USER | xargs echo)
fi

if [ -e $dockerSecretDir/ALERT_RABBIT_PASSWORD ];
then
  echo "RabbitMQ password secret set; using value from secret."
  export ALERT_RABBIT_PASSWORD=$(cat $dockerSecretDir/ALERT_RABBIT_PASSWORD | xargs echo)
fi

if [ -e $dockerSecretDir/ALERT_DB_USERNAME ];
then
  echo "Alert Database user secret set; using value from secret."
  alertDatabaseUser=$(cat $dockerSecretDir/ALERT_DB_USERNAME | xargs echo)
  export ALERT_DB_USERNAME=$alertDatabaseUser

  alertDatabaseAdminUser=$alertDatabaseUser
  export ALERT_DB_ADMIN_USERNAME=$alertDatabaseAdminUser
  echo "Alert Database user variable set to secret value."
fi

if [ -e $dockerSecretDir/ALERT_DB_PASSWORD ];
then
  echo "Alert Database password secret set; using value from secret."
  alertDatabasePassword=$(cat $dockerSecretDir/ALERT_DB_PASSWORD | xargs echo)
  export ALERT_DB_PASSWORD=$alertDatabasePassword

  alertDatabaseAdminPassword=$alertDatabasePassword
  export ALERT_DB_ADMIN_PASSWORD=$alertDatabaseAdminPassword
  echo "Alert Database password variable set to secret value."
fi

if [ -e $dockerSecretDir/ALERT_DB_ADMIN_USERNAME ];
then
  echo "Alert Database admin user secret set; using value from secret."
  alertDatabaseAdminUser=$(cat $dockerSecretDir/ALERT_DB_ADMIN_USERNAME | xargs echo)
  export ALERT_DB_ADMIN_USERNAME=$alertDatabaseAdminUser
  echo "Alert Database admin user variable set to secret value."
fi

if [ -e $dockerSecretDir/ALERT_DB_ADMIN_PASSWORD ];
then
  echo "Alert Database admin password secret set; using value from secret."
  alertDatabaseAdminPassword=$(cat $dockerSecretDir/ALERT_DB_ADMIN_PASSWORD | xargs echo)
  export ALERT_DB_ADMIN_PASSWORD=$alertDatabaseAdminPassword
  echo "Alert Database admin password variable set to secret value."
fi

if [ -e $dockerSecretDir/ALERT_DB_SSL_KEY_PATH ];
then
  echo "Alert Database SSL key set; using value from secret."
  alertDatabaseSslKey=$dockerSecretDir/ALERT_DB_SSL_KEY_PATH
  export ALERT_DB_SSL_KEY_PATH=$alertDatabaseSslKey
  echo "Alert Database SSL key variable set to secret value."
fi

if [ -e $dockerSecretDir/ALERT_DB_SSL_CERT_PATH ];
then
  echo "Alert Database SSL key set; using value from secret."
  alertDatabaseSslCert=$dockerSecretDir/ALERT_DB_SSL_CERT_PATH
  export ALERT_DB_SSL_CERT_PATH=$alertDatabaseSslCert
  echo "Alert Database SSL cert variable set to secret value."
fi

if [ -e $dockerSecretDir/ALERT_DB_SSL_ROOT_CERT_PATH ];
then
  echo "Alert Database SSL key set; using value from secret."
  alertDatabaseSslRootCert=$dockerSecretDir/ALERT_DB_SSL_ROOT_CERT_PATH
  export ALERT_DB_SSL_ROOT_CERT_PATH=$alertDatabaseSslRootCert
  echo "Alert Database SSL root cert variable set to secret value."
fi

createCertificateStoreDirectory() {
  echo "Checking certificate store directory"
  if [ -d ${SECURITY_DIR} ];
  then
    echo "Certificate store directory ${SECURITY_DIR} exists"
  else
    mkdir -p -v ${SECURITY_DIR}
  fi
}

manageRootCertificate() {
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh root \
        --ca $targetCAHost:$targetCAPort \
        --outputDirectory ${SECURITY_DIR} \
        --profile peer
}

manageSelfSignedServerCertificate() {
    echo "Attempting to generate $APPLICATION_NAME self-signed server certificate and key."
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh server-cert \
        --ca $targetCAHost:$targetCAPort \
        --rootcert ${SECURITY_DIR}/root.crt \
        --key ${SECURITY_DIR}/$serverCertName.key \
        --cert ${SECURITY_DIR}/$serverCertName.crt \
        --outputDirectory ${SECURITY_DIR} \
        --commonName $serverCertName \
        --san $targetWebAppHost \
        --san $alertHostName \
        --san localhost \
        --hostName $targetWebAppHost
    exitCode=$?
    if [ $exitCode -eq 0 ];
    then
      echo "Generated $APPLICATION_NAME self-signed server certificate and key."
      chmod 644 ${SECURITY_DIR}/root.crt
      chmod 400 ${SECURITY_DIR}/$serverCertName.key
      chmod 644 ${SECURITY_DIR}/$serverCertName.crt
    else
      echo "ERROR: Unable to generate $APPLICATION_NAME self-signed server certificate and key (Code: $exitCode)."
      exit $exitCode
    fi
}

manageBlackduckSystemClientCertificate() {
    echo "Attempting to generate blackduck_system client certificate and key."
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh client-cert \
        --ca $targetCAHost:$targetCAPort \
        --outputDirectory ${SECURITY_DIR} \
        --commonName blackduck_system
    exitCode=$?
    if [ $exitCode -eq 0 ];
    then
        chmod 400 ${SECURITY_DIR}/blackduck_system.key
        chmod 644 ${SECURITY_DIR}/blackduck_system.crt
    else
        echo "ERROR: Unable to generate blackduck_system certificate and key (Code: $exitCode)."
        exit $exitCode
    fi

    echo "Attempting to generate blackduck_system store."
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh keystore \
        --outputDirectory ${SECURITY_DIR} \
        --outputFile blackduck_system.keystore \
        --password changeit \
        --keyAlias blackduck_system \
        --key ${SECURITY_DIR}/blackduck_system.key \
        --cert ${SECURITY_DIR}/blackduck_system.crt
    exitCode=$?
    if [ $exitCode -ne 0 ];
    then
        echo "ERROR: Unable to generate blackduck_system store (Code: $exitCode)."
        exit $exitCode
    fi

    echo "Attempting to trust root certificate within the blackduck_system store."
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh trust-java-cert \
        --store ${SECURITY_DIR}/blackduck_system.keystore \
        --password changeit \
        --cert ${SECURITY_DIR}/root.crt \
        --certAlias blackduck_root
    exitCode=$?
    if [ $exitCode -ne 0 ];
    then
      echo "ERROR: Unable to trust root certificate within the blackduck_system store (Code: $exitCode)."
      exit $exitCode
    fi
}

createTruststore() {
    if [ -f $dockerSecretDir/jssecacerts ];
    then
        echo "Custom jssecacerts file found."
        echo "Copying file jssecacerts to the certificate location"
        cp $dockerSecretDir/jssecacerts $truststoreFile
    elif [ -f $dockerSecretDir/cacerts ];
    then
        echo "Custom cacerts file found."
        echo "Copying file cacerts to the certificate location"
        cp $dockerSecretDir/cacerts $truststoreFile
    else
        echo "Attempting to copy Java cacerts to create truststore."
        ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh truststore --outputDirectory ${SECURITY_DIR} --outputFile $APPLICATION_NAME.truststore
        exitCode=$?
        if [ ! $exitCode -eq 0 ];
        then
            echo "Unable to create truststore (Code: $exitCode)."
            exit $exitCode
        fi
    fi
}

trustRootCertificate() {
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh trust-java-cert \
                        --store $truststoreFile \
                        --password $truststorePassword \
                        --cert ${SECURITY_DIR}/root.crt \
                        --certAlias hub-root

    exitCode=$?
    if [ $exitCode -eq 0 ];
    then
        echo "Successfully imported BlackDuck root certificate into Java truststore."
    else
        echo "Unable to import BlackDuck root certificate into Java truststore (Code: $exitCode)."
        exit $exitCode
    fi
}

trustBlackDuckSystemCertificate() {
  ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh trust-java-cert \
                        --store $truststoreFile \
                        --password $truststorePassword \
                        --cert ${SECURITY_DIR}/blackduck_system.crt \
                        --certAlias blackduck_system

    exitCode=$?
    if [ $exitCode -eq 0 ];
    then
        echo "Successfully imported BlackDuck root certificate into Java truststore."
    else
        echo "Unable to import BlackDuck root certificate into Java truststore (Code: $exitCode)."
        exit $exitCode
    fi
}

trustProxyCertificate() {
    proxyCertificate="$dockerSecretDir/HUB_PROXY_CERT_FILE"

    if [ ! -f "$dockerSecretDir/HUB_PROXY_CERT_FILE" ];
    then
        echo "WARNING: Proxy certificate file is not found in secret. Skipping Proxy Certificate Import."
    else
        ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh trust-java-cert \
                                --store $truststoreFile \
                                --password $truststorePassword \
                                --cert $proxyCertificate \
                                --certAlias proxycert
        exitCode=$?
        if [ $exitCode -eq 0 ];
        then
            echo "Successfully imported proxy certificate into Java truststore."
        else
            echo "Unable to import proxy certificate into Java truststore (Code: $exitCode)."
        fi
    fi
}

createKeystore() {
    certKey=${SECURITY_DIR}/$serverCertName.key
    certFile=${SECURITY_DIR}/$serverCertName.crt
    if [ -f $dockerSecretDir/WEBSERVER_CUSTOM_CERT_FILE ] && [ -f $dockerSecretDir/WEBSERVER_CUSTOM_KEY_FILE ];
    then
        certKey="${dockerSecretDir}/WEBSERVER_CUSTOM_KEY_FILE"
        certFile="${dockerSecretDir}/WEBSERVER_CUSTOM_CERT_FILE"

        echo "Custom webserver cert and key found"
        echo "Using $certFile and $certKey for webserver"
    fi
    # Create the keystore with given private key and certificate.
    echo "Attempting to create keystore."
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh keystore \
                                             --outputDirectory ${SECURITY_DIR} \
                                             --outputFile $keyStoreFile \
                                             --password $keystorePassword \
                                             --keyAlias $APPLICATION_NAME \
                                             --key $certKey \
                                             --cert $certFile
    exitCode=$?
    if [ $exitCode -eq 0 ];
    then
        chmod 644 $keystoreFilePath
    else
        echo "Unable to create keystore (Code: $exitCode)."
        exit $exitCode
    fi
}

importBlackDuckSystemCertificateIntoKeystore() {
  ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh trust-java-cert \
                        --store $keystoreFilePath \
                        --password $keystorePassword \
                        --cert ${SECURITY_DIR}/blackduck_system.crt \
                        --certAlias blackduck_system

    exitCode=$?
    if [ $exitCode -eq 0 ];
    then
        echo "Successfully imported BlackDuck system certificate into Java keystore."
    else
        echo "Unable to import BlackDuck system certificate into Java keystore (Code: $exitCode)."
        exit $exitCode
    fi
}
# Bootstrap will optionally configure the config volume if it hasn't been configured yet.
# After that we verify, and then launch the webserver.

importDockerHubServerCertificate() {
    if keytool -list -keystore "$truststoreFile" -storepass $truststorePassword -alias "hub.docker.com"
    then
        echo "The Docker Hub certificate is already imported."
    else
        if keytool -printcert -rfc -sslserver "hub.docker.com" -v | keytool -importcert -keystore "$truststoreFile" -storepass $truststorePassword -alias "hub.docker.com" -noprompt
        then
            echo "Completed importing Docker Hub certificate."
        else
            echo "Unable to add the Docker Hub certificate. Please try to import the certificate manually."
        fi
    fi
}

liquibaseChangelockReset() {
  echo "Begin releasing liquibase changeloglock."
  $JAVA_HOME/bin/java -cp "${ALERT_TAR_HOME}/lib/liquibase/*" \
  liquibase.integration.commandline.Main \
  --url="jdbc:h2:file:${alertDatabaseDir}" \
  --username="sa" \
  --password="" \
  --driver="org.h2.Driver" \
  --changeLogFile="${upgradeResourcesDir}/release-locks-changelog.xml" \
  releaseLocks
  echo "End releasing liquibase changeloglock."
}

validatePostgresConnection() {
  # Since the database is now external to the alert container verify we can connect to the database before starting.
  # https://stackoverflow.com/a/58784528/6921621
    echo "Checking for postgres connectivity... "
    if psql "${alertDatabaseConfig}" -c '\l' > /dev/null;
    then
      echo "Alert postgres database connection valid."
    else
      echo "Alert postgres connection cannot be made."
      sleep 10
      exit 1
    fi
}

createPostgresDatabase() {
  # Since the database is now external to the alert container check if the database, schema, and tables have been created for alert.
  # https://stackoverflow.com/a/58784528/6921621
    echo "Checking if $alertDatabaseName exists... "
    if  psql "${alertDatabaseConfig}" -c '\l' |grep -q "$alertDatabaseName";
    then
        echo "Alert postgres database exists."
        if psql "${alertDatabaseConfig}" -c '\dt ALERT.*' |grep -q 'field_values';
        then
            echo "Alert postgres database tables have been successfully created."
        else
            echo "Alert postgres database tables have not been created. Creating database tables for database: $alertDatabaseName "
            psql "${alertDatabaseConfig}" -f ${upgradeResourcesDir}/init_alert_db.sql
        fi
    else
        echo "Alert postgres database does not exist. Please create the database: $alertDatabaseName"
        sleep 10
        exit 1
    fi
}

validatePostgresDatabase() {
    # https://stackoverflow.com/a/58784528/6921621
    echo "Checking for postgres databases... "
    if  psql "${alertDatabaseConfig}" -c '\l' | grep -q "$alertDatabaseName";
    then
        echo "Alert postgres database exists."
        if psql "${alertDatabaseConfig}" -c '\dt ALERT.*' |grep -q 'field_values';
        then
            echo "Alert postgres database tables have been successfully created."
        else
            echo "Alert postgres database tables have not been created."
            sleep 10
            exit 1
        fi
    else
        echo "Alert postgres database does not exist."
        sleep 10
        exit 1
    fi
}

postgresPrepare600Upgrade() {
    echo "Determining if preparation for 6.0.0 upgrade is necessary..."
    if psql "${alertDatabaseConfig}" -c 'SELECT COUNT(CONTEXT) FROM Alert.Config_Contexts;' | grep -q '2';
    then
        echo "Alert postgres database is initialized."
    else
        echo "Preparing the old Alert database to be upgraded to 6.0.0..."
        if [ -f "${ALERT_DATA_DIR}/alertdb.mv.db" ];
        then
            echo "A previous database existed."
            liquibaseChangelockReset
            echo "Clearing old checksums for offline upgrade..."
            ${JAVA_HOME}/bin/java -cp "${ALERT_TAR_HOME}/lib/liquibase/*" \
            liquibase.integration.commandline.Main \
            --url="jdbc:h2:file:${alertDatabaseDir}" \
            --username="sa" \
            --password="" \
            --driver="org.h2.Driver" \
            --changeLogFile="${upgradeResourcesDir}/changelog-master.xml" \
            clearCheckSums

            echo "Upgrading old database to 5.3.0 so that it can be properly exported..."
            ${JAVA_HOME}/bin/java -cp "${ALERT_TAR_HOME}/lib/liquibase/*" \
            liquibase.integration.commandline.Main \
            --url="jdbc:h2:file:${alertDatabaseDir}" \
            --username="sa" \
            --password="" \
            --driver="org.h2.Driver" \
            --changeLogFile="${upgradeResourcesDir}/changelog-master.xml" \
            update

            echo "Creating temp directory for data migration..."
            mkdir -m 766 ${ALERT_DATA_DIR}/temp

            echo "Exporting data from old database..."
            $JAVA_HOME/bin/java -cp "${ALERT_TAR_HOME}/lib/liquibase/*" \
            org.h2.tools.RunScript \
            -url "jdbc:h2:${alertDatabaseDir}" \
            -user "sa" \
            -password "" \
            -driver "org.h2.Driver" \
            -script ${upgradeResourcesDir}/export_h2_tables.sql

            chmod 766 ${ALERT_DATA_DIR}/temp/*

            echo "Importing data from old database into new database..."
            psql "${alertDatabaseConfig}" -f ${upgradeResourcesDir}/import_postgres_tables.sql
        else
            echo "No previous database existed."
        fi
    fi
}

createPostgresExtensions() {
  echo "Creating required postgres extensions."
  psql "${alertDatabaseAdminConfig}" -f ${upgradeResourcesDir}/create_extension.sql
}

alertDatabaseAdminConfig="host=$alertDatabaseHost port=$alertDatabasePort dbname=$alertDatabaseName user=$alertDatabaseAdminUser password=$alertDatabaseAdminPassword sslmode=$alertDatabaseSslMode sslkey=$alertDatabaseSslKey sslcert=$alertDatabaseSslCert sslrootcert=$alertDatabaseSslRootCert"
alertDatabaseConfig="host=$alertDatabaseHost port=$alertDatabasePort dbname=$alertDatabaseName user=$alertDatabaseUser password=$alertDatabasePassword sslmode=$alertDatabaseSslMode sslkey=$alertDatabaseSslKey sslcert=$alertDatabaseSslCert sslrootcert=$alertDatabaseSslRootCert"

echo "Alert max heap size: $ALERT_MAX_HEAP_SIZE"
echo "Certificate authority host: $targetCAHost"
echo "Certificate authority port: $targetCAPort"

if [ ! -f "${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh" ];
then
  echo "ERROR: certificate management script is not present."
  sleep 10
  exit 1
else
  validatePostgresConnection
  createCertificateStoreDirectory
  if [ -f $dockerSecretDir/WEBSERVER_CUSTOM_CERT_FILE ] && [ -f $dockerSecretDir/WEBSERVER_CUSTOM_KEY_FILE ];
  then
    echo "Custom webserver cert and key found"
    manageRootCertificate
  else
      manageSelfSignedServerCertificate
  fi
  manageBlackduckSystemClientCertificate
  createTruststore
  trustRootCertificate
  trustBlackDuckSystemCertificate
  trustProxyCertificate
  createKeystore
  importBlackDuckSystemCertificateIntoKeystore
  importDockerHubServerCertificate
  createPostgresDatabase
  validatePostgresDatabase
  postgresPrepare600Upgrade
  createPostgresExtensions
  liquibaseChangelockReset

  if [ -f "$truststoreFile" ];
  then
      JAVA_OPTS="$JAVA_OPTS -Xmx$ALERT_MAX_HEAP_SIZE -Djavax.net.ssl.trustStore=$truststoreFile"
      export JAVA_OPTS
  fi
fi

exec "$@"
