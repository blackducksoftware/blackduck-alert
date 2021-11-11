#!/bin/sh

certificateManagerDir=/opt/blackduck/alert/bin
securityDir=/opt/blackduck/alert/security
alertHome=/opt/blackduck/alert
alertConfigHome=${alertHome}/alert-config
alertDataDir=${alertConfigHome}/data
alertDatabaseDir=${alertDataDir}/alertdb
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
upgradeResourcesDir=$alertHome/alert-tar/upgradeResources

serverCertName=$APPLICATION_NAME-server

dockerSecretDir=${RUN_SECRETS_DIR:-/run/secrets}
keyStoreFile=$APPLICATION_NAME.keystore
keystoreFilePath=$securityDir/$keyStoreFile
keystorePassword="${ALERT_KEY_STORE_PASSWORD:-changeit}"
truststoreFile=$securityDir/$APPLICATION_NAME.truststore
truststorePassword="${ALERT_TRUST_STORE_PASSWORD:-changeit}"
truststoreType="${ALERT_TRUST_STORE_TYPE:-JKS}"

alertHostName="${ALERT_HOSTNAME:-localhost}"
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

if [ -e $dockerSecretDir/ALERT_DB_USERNAME ];
then
  echo "Alert Database user secret set; using value from secret."
  alertDatabaseUser=$(cat $dockerSecretDir/ALERT_DB_USERNAME | xargs echo)
  export ALERT_DB_USERNAME=$alertDatabaseUser;

  alertDatabaseAdminUser=$alertDatabaseUser;
  export ALERT_DB_ADMIN_USERNAME=$alertDatabaseAdminUser;
  echo "Alert Database user variable set to secret value."
fi

if [ -e $dockerSecretDir/ALERT_DB_PASSWORD ];
then
  echo "Alert Database password secret set; using value from secret."
  alertDatabasePassword=$(cat $dockerSecretDir/ALERT_DB_PASSWORD | xargs echo)
  export ALERT_DB_PASSWORD=$alertDatabasePassword;

  alertDatabaseAdminPassword=$alertDatabasePassword;
  export ALERT_DB_ADMIN_PASSWORD=$alertDatabaseAdminPassword;
  echo "Alert Database password variable set to secret value."
fi

if [ -e $dockerSecretDir/ALERT_DB_ADMIN_USERNAME ];
then
  echo "Alert Database admin user secret set; using value from secret."
  alertDatabaseAdminUser=$(cat $dockerSecretDir/ALERT_DB_ADMIN_USERNAME | xargs echo)
  export ALERT_DB_ADMIN_USERNAME=$alertDatabaseAdminUser;
  echo "Alert Database admin user variable set to secret value."
fi

if [ -e $dockerSecretDir/ALERT_DB_ADMIN_PASSWORD ];
then
  echo "Alert Database admin password secret set; using value from secret."
  alertDatabaseAdminPassword=$(cat $dockerSecretDir/ALERT_DB_ADMIN_PASSWORD | xargs echo)
  export ALERT_DB_ADMIN_PASSWORD=$alertDatabaseAdminPassword;
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
  export ALERT_DB_SSL_ROOT_CERT_PATH=alertDatabaseSslRootCert
  echo "Alert Database SSL root cert variable set to secret value."
fi

alertDatabaseAdminConfig="host=$alertDatabaseHost port=$alertDatabasePort dbname=$alertDatabaseName user=$alertDatabaseAdminUser password=$alertDatabaseAdminPassword sslmode=$alertDatabaseSslMode sslkey=$alertDatabaseSslKey sslcert=$alertDatabaseSslCert sslrootcert=$alertDatabaseSslRootCert"
alertDatabaseConfig="host=$alertDatabaseHost port=$alertDatabasePort dbname=$alertDatabaseName user=$alertDatabaseUser password=$alertDatabasePassword sslmode=$alertDatabaseSslMode sslkey=$alertDatabaseSslKey sslcert=$alertDatabaseSslCert sslrootcert=$alertDatabaseSslRootCert"

echo "Alert max heap size: $ALERT_MAX_HEAP_SIZE"
echo "Certificate authority host: $targetCAHost"
echo "Certificate authority port: $targetCAPort"

createCertificateStoreDirectory() {
  echo "Checking certificate store directory"
  if [ -d $securityDir ];
  then
    echo "Certificate store directory $securityDir exists"
  else
    mkdir -p -v $securityDir
  fi
}

manageRootCertificate() {
    $certificateManagerDir/certificate-manager.sh root \
        --ca $targetCAHost:$targetCAPort \
        --outputDirectory $securityDir \
        --profile peer
}

manageSelfSignedServerCertificate() {
    echo "Attempting to generate $APPLICATION_NAME self-signed server certificate and key."
    $certificateManagerDir/certificate-manager.sh server-cert \
        --ca $targetCAHost:$targetCAPort \
        --rootcert $securityDir/root.crt \
        --key $securityDir/$serverCertName.key \
        --cert $securityDir/$serverCertName.crt \
        --outputDirectory $securityDir \
        --commonName $serverCertName \
        --san $targetWebAppHost \
        --san $alertHostName \
        --san localhost \
        --hostName $targetWebAppHost
    exitCode=$?
    if [ $exitCode -eq 0 ];
    then
      echo "Generated $APPLICATION_NAME self-signed server certificate and key."
      chmod 644 $securityDir/root.crt
      chmod 400 $securityDir/$serverCertName.key
      chmod 644 $securityDir/$serverCertName.crt
    else
      echo "ERROR: Unable to generate $APPLICATION_NAME self-signed server certificate and key (Code: $exitCode)."
      exit $exitCode
    fi
}

manageBlackduckSystemClientCertificate() {
    echo "Attempting to generate blackduck_system client certificate and key."
    $certificateManagerDir/certificate-manager.sh client-cert \
        --ca $targetCAHost:$targetCAPort \
        --outputDirectory $securityDir \
        --commonName blackduck_system
    exitCode=$?
    if [ $exitCode -eq 0 ];
    then
        chmod 400 $securityDir/blackduck_system.key
        chmod 644 $securityDir/blackduck_system.crt
    else
        echo "ERROR: Unable to generate blackduck_system certificate and key (Code: $exitCode)."
        exit $exitCode
    fi

    echo "Attempting to generate blackduck_system store."
    $certificateManagerDir/certificate-manager.sh keystore \
        --outputDirectory $securityDir \
        --outputFile blackduck_system.keystore \
        --password changeit \
        --keyAlias blackduck_system \
        --key $securityDir/blackduck_system.key \
        --cert $securityDir/blackduck_system.crt
    exitCode=$?
    if [ $exitCode -ne 0 ];
    then
        echo "ERROR: Unable to generate blackduck_system store (Code: $exitCode)."
        exit $exitCode
    fi

    echo "Attempting to trust root certificate within the blackduck_system store."
    $certificateManagerDir/certificate-manager.sh trust-java-cert \
        --store $securityDir/blackduck_system.keystore \
        --password changeit \
        --cert $securityDir/root.crt \
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
        cp $dockerSecretDir/jssecacerts $securityDir/$APPLICATION_NAME.truststore
    elif [ -f $dockerSecretDir/cacerts ];
    then
        echo "Custom cacerts file found."
        echo "Copying file cacerts to the certificate location"
        cp $dockerSecretDir/cacerts $securityDir/$APPLICATION_NAME.truststore
    else
        echo "Attempting to copy Java cacerts to create truststore."
        $certificateManagerDir/certificate-manager.sh truststore --outputDirectory $securityDir --outputFile $APPLICATION_NAME.truststore
        exitCode=$?
        if [ ! $exitCode -eq 0 ];
        then
            echo "Unable to create truststore (Code: $exitCode)."
            exit $exitCode
        fi
    fi
}

trustRootCertificate() {
    $certificateManagerDir/certificate-manager.sh trust-java-cert \
                        --store $truststoreFile \
                        --password $truststorePassword \
                        --cert $securityDir/root.crt \
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
  $certificateManagerDir/certificate-manager.sh trust-java-cert \
                        --store $truststoreFile \
                        --password $truststorePassword \
                        --cert $securityDir/blackduck_system.crt \
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
        $certificateManagerDir/certificate-manager.sh trust-java-cert \
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
    certKey=$securityDir/$serverCertName.key
    certFile=$securityDir/$serverCertName.crt
    if [ -f $dockerSecretDir/WEBSERVER_CUSTOM_CERT_FILE ] && [ -f $dockerSecretDir/WEBSERVER_CUSTOM_KEY_FILE ];
    then
        certKey="${dockerSecretDir}/WEBSERVER_CUSTOM_KEY_FILE"
        certFile="${dockerSecretDir}/WEBSERVER_CUSTOM_CERT_FILE"

        echo "Custom webserver cert and key found"
    	echo "Using $certFile and $certKey for webserver"
    fi
    # Create the keystore with given private key and certificate.
    echo "Attempting to create keystore."
    $certificateManagerDir/certificate-manager.sh keystore \
                                             --outputDirectory $securityDir \
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
  $certificateManagerDir/certificate-manager.sh trust-java-cert \
                        --store $keystoreFilePath \
                        --password $keystorePassword \
                        --cert $securityDir/blackduck_system.crt \
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
# Bootstrap will optionally configure the config volume if it hasnt been configured yet.
# After that we verify, and then launch the webserver.


importDockerHubServerCertificate(){
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
  $JAVA_HOME/bin/java -cp "$alertHome/alert-tar/lib/liquibase/*" \
  liquibase.integration.commandline.Main \
  --url="jdbc:h2:file:$alertDatabaseDir" \
  --username="sa" \
  --password="" \
  --driver="org.h2.Driver" \
  --changeLogFile="$upgradeResourcesDir/release-locks-changelog.xml" \
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
        if [ -f "${alertDataDir}/alertdb.mv.db" ];
        then
            echo "A previous database existed."
            liquibaseChangelockReset
            echo "Clearing old checksums for offline upgrade..."
            ${JAVA_HOME}/bin/java -cp "$alertHome/alert-tar/lib/liquibase/*" \
            liquibase.integration.commandline.Main \
            --url="jdbc:h2:file:${alertDatabaseDir}" \
            --username="sa" \
            --password="" \
            --driver="org.h2.Driver" \
            --changeLogFile="${upgradeResourcesDir}/changelog-master.xml" \
            clearCheckSums

            echo "Upgrading old database to 5.3.0 so that it can be properly exported..."
            ${JAVA_HOME}/bin/java -cp "$alertHome/alert-tar/lib/liquibase/*" \
            liquibase.integration.commandline.Main \
            --url="jdbc:h2:file:${alertDatabaseDir}" \
            --username="sa" \
            --password="" \
            --driver="org.h2.Driver" \
            --changeLogFile="${upgradeResourcesDir}/changelog-master.xml" \
            update

            echo "Creating temp directory for data migration..."
            mkdir ${alertConfigHome}/data/temp
            chmod 766 ${alertConfigHome}/data/temp

            echo "Exporting data from old database..."
            $JAVA_HOME/bin/java -cp "${alertHome}/alert-tar/lib/liquibase/*" \
            org.h2.tools.RunScript \
            -url "jdbc:h2:${alertDatabaseDir}" \
            -user "sa" \
            -password "" \
            -driver "org.h2.Driver" \
            -script ${upgradeResourcesDir}/export_h2_tables.sql

            chmod 766 ${alertConfigHome}/data/temp/*

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

if [ ! -f "$certificateManagerDir/certificate-manager.sh" ];
then
  echo "ERROR: certificate management script is not present."
  sleep 10
  exit 1;
else
  validatePostgresConnection
  createCertificateStoreDirectory
  if [ -f $secretsMountPath/WEBSERVER_CUSTOM_CERT_FILE ] && [ -f $secretsMountPath/WEBSERVER_CUSTOM_KEY_FILE ];
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
