#!/bin/sh

logIt() {
  echo "$(date "+%F %T") :: ${1}"
}

logIt "Launching ${0}"

## ALERT VARIABLES ##
export BLACKDUCK_ALERT_OPTS="${DEFAULT_BLACKDUCK_ALERT_OPTS} ${BLACKDUCK_ALERT_OPTS}"
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
serverCertName=${APPLICATION_NAME}-server
dockerSecretDir=${RUN_SECRETS_DIR:-/run/secrets}
keyStoreFile=${APPLICATION_NAME}.keystore
keystoreFilePath=${SECURITY_DIR}/$keyStoreFile
keystorePassword="${ALERT_KEY_STORE_PASSWORD:-changeit}"
truststoreFile=${SECURITY_DIR}/${APPLICATION_NAME}.truststore
truststorePassword="${ALERT_TRUST_STORE_PASSWORD:-changeit}"

## OTHER VARIABLES ##
targetCAHost="${HUB_CFSSL_HOST:-cfssl}"
targetCAPort="${HUB_CFSSL_PORT:-8888}"
targetWebAppHost="${HUB_WEBAPP_HOST:-alert}"

checkStatus() {
  if [ "${1}" -ne 0 ];
  then
    logIt "ERROR: ${2} (Code: ${1})."
    sleep 15
    exit ${1}
  fi
  logIt "SUCCESS: ${2}."
}

createCertificateStoreDirectory() {
  mkdir -p "${SECURITY_DIR}"
  checkStatus $? "Making ${SECURITY_DIR}"
}

manageRootCertificate() {
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh root \
        --ca $targetCAHost:$targetCAPort \
        --outputDirectory ${SECURITY_DIR} \
        --profile peer
    checkStatus $? "Executing ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh"
}

manageSelfSignedServerCertificate() {
    logIt "Attempting to generate ${APPLICATION_NAME} self-signed server certificate and key."
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
    checkStatus $? "Generating ${APPLICATION_NAME} self-signed server certificate and key"

    chmod 644 ${SECURITY_DIR}/root.crt
    chmod 400 ${SECURITY_DIR}/$serverCertName.key
    chmod 644 ${SECURITY_DIR}/$serverCertName.crt
}

manageCertificate() {
    if [ -f "${dockerSecretDir}/WEBSERVER_CUSTOM_CERT_FILE" ] && [ -f "${dockerSecretDir}/WEBSERVER_CUSTOM_KEY_FILE" ];
    then
      logIt "Custom webserver cert and key found"
      manageRootCertificate
    else
      manageSelfSignedServerCertificate
    fi
}

manageBlackduckSystemClientCertificate() {
    logIt "Attempting to generate blackduck_system client certificate and key."
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh client-cert \
        --ca $targetCAHost:$targetCAPort \
        --outputDirectory ${SECURITY_DIR} \
        --commonName blackduck_system
    checkStatus $? "Generating blackduck_system certificate and key"

    chmod 400 ${SECURITY_DIR}/blackduck_system.key
    chmod 644 ${SECURITY_DIR}/blackduck_system.crt

    logIt "Attempting to generate blackduck_system store."
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh keystore \
        --outputDirectory ${SECURITY_DIR} \
        --outputFile blackduck_system.keystore \
        --password changeit \
        --keyAlias blackduck_system \
        --key ${SECURITY_DIR}/blackduck_system.key \
        --cert ${SECURITY_DIR}/blackduck_system.crt
    checkStatus $? "Generating ${SECURITY_DIR}/blackduck_system.keystore"

    logIt "Attempting to trust root certificate within the blackduck_system store."
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh trust-java-cert \
        --store ${SECURITY_DIR}/blackduck_system.keystore \
        --password changeit \
        --cert ${SECURITY_DIR}/root.crt \
        --certAlias blackduck_root
    checkStatus $? "Trust root certificate within ${SECURITY_DIR}/blackduck_system.keystore"
}

createTruststore() {
    if [ -f $dockerSecretDir/jssecacerts ];
    then
        logIt "Custom jssecacerts file found."
        cp $dockerSecretDir/jssecacerts $truststoreFile
        checkStatus $? "Coping ${dockerSecretDir}/jssecacerts"
    elif [ -f $dockerSecretDir/cacerts ];
    then
        logIt "Custom cacerts file found."
        cp $dockerSecretDir/cacerts $truststoreFile
        checkStatus $? "Coping ${dockerSecretDir}/cacerts"
    else
        logIt "Attempting to create ${APPLICATION_NAME}.truststore."
        ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh truststore --outputDirectory ${SECURITY_DIR} --outputFile ${APPLICATION_NAME}.truststore
        checkStatus $? "Create ${APPLICATION_NAME}.truststore"
    fi
}

trustRootCertificate() {
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh trust-java-cert \
                        --store $truststoreFile \
                        --password $truststorePassword \
                        --cert ${SECURITY_DIR}/root.crt \
                        --certAlias hub-root
    checkStatus $? "Import ${SECURITY_DIR}/root.crt into $truststoreFile"
}

trustBlackDuckSystemCertificate() {
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh trust-java-cert \
                        --store $truststoreFile \
                        --password $truststorePassword \
                        --cert ${SECURITY_DIR}/blackduck_system.crt \
                        --certAlias blackduck_system
    checkStatus $? "Import ${SECURITY_DIR}/blackduck_system.crt into $truststoreFile"
}

trustProxyCertificate() {
    proxyCertificate="$dockerSecretDir/HUB_PROXY_CERT_FILE"

    if [ ! -f "${proxyCertificate}" ];
    then
        logIt "WARNING: Proxy certificate file is not found in secret. Skipping Proxy Certificate Import."
    else
        ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh trust-java-cert \
                                --store $truststoreFile \
                                --password $truststorePassword \
                                --cert $proxyCertificate \
                                --certAlias proxycert
        exitCode=$?
        if [ $exitCode -eq 0 ];
        then
            logIt "Successfully imported proxy certificate into Java truststore."
        else
            logIt "Unable to import proxy certificate into Java truststore (Code: $exitCode)."
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

        logIt "Custom webserver cert and key found"
        logIt "Using $certFile and $certKey for webserver"
    fi
    # Create the keystore with given private key and certificate.
    logIt "Attempting to create keystore."
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh keystore \
                                             --outputDirectory ${SECURITY_DIR} \
                                             --outputFile $keyStoreFile \
                                             --password $keystorePassword \
                                             --keyAlias ${APPLICATION_NAME} \
                                             --key $certKey \
                                             --cert $certFile
    checkStatus $? "Create ${SECURITY_DIR}/$keyStoreFile"
}

importBlackDuckSystemCertificateIntoKeystore() {
    ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh trust-java-cert \
                        --store $keystoreFilePath \
                        --password $keystorePassword \
                        --cert ${SECURITY_DIR}/blackduck_system.crt \
                        --certAlias blackduck_system
    checkStatus $? "Import ${SECURITY_DIR}/blackduck_system.crt into $keystoreFilePath"
}

importDockerHubServerCertificate() {
    if "${JAVA_HOME}/bin/keytool" -list -keystore "$truststoreFile" -storepass $truststorePassword -alias "hub.docker.com";
    then
        logIt "The Docker Hub certificate is already imported."
    else
        if "${JAVA_HOME}/bin/keytool" -printcert -rfc -sslserver "hub.docker.com" -v | "${JAVA_HOME}/bin/keytool" -importcert -keystore "$truststoreFile" -storepass $truststorePassword -alias "hub.docker.com" -noprompt;
        then
            logIt "Completed importing Docker Hub certificate."
        else
            logIt "Unable to add the Docker Hub certificate. Please try to import the certificate manually."
        fi
    fi
}

liquibaseChangelockReset() {
  logIt "Begin releasing liquibase changeloglock."
  "${JAVA_HOME}/bin/java" -cp "${ALERT_TAR_HOME}/lib/liquibase/*" \
      liquibase.integration.commandline.Main \
      --url="jdbc:h2:file:${alertDatabaseDir}" \
      --username="sa" \
      --password="" \
      --driver="org.h2.Driver" \
      --changeLogFile="${upgradeResourcesDir}/release-locks-changelog.xml" \
      releaseLocks
  logIt "End releasing liquibase changeloglock."
}

validatePostgresConnection() {
    # Since the database is now external to the alert container verify we can connect to the database before starting.
    # https://stackoverflow.com/a/58784528/6921621

    psql "${alertDatabaseConfig}" -c '\l' > /dev/null
    checkStatus $? "Validate postgres connection"
}

validateAlertDBExists() {
    psql "${alertDatabaseConfig}" -c '\l' | grep -q "${alertDatabaseName}"
    checkStatus $? "Validate database '${alertDatabaseName}' exists"
}

createPostgresDatabase() {
    # Since the database is now external to the alert container check if the database, schema, and tables have been created for alert.
    # https://stackoverflow.com/a/58784528/6921621

    validateAlertDBExists

    if psql "${alertDatabaseConfig}" -c '\dt ALERT.*' | grep -q 'field_values';
    then
        logIt "Alert postgres database tables have been successfully created."
    else
        logIt "Alert postgres database tables have not been created. Creating database tables for database: ${alertDatabaseName}"
        psql "${alertDatabaseConfig}" -f ${upgradeResourcesDir}/init_alert_db.sql
        checkStatus $? "Creating database tables for database: ${alertDatabaseName}"
    fi
}

validatePostgresDatabase() {
    # https://stackoverflow.com/a/58784528/6921621

    validateAlertDBExists
    psql "${alertDatabaseConfig}" -c '\dt ALERT.*' | grep -q 'field_values'
    checkStatus $? "Creating Alert postgres database tables"
}

postgresPrepare600Upgrade() {
    logIt "Determining if preparation for 6.0.0 upgrade is necessary..."
    if psql "${alertDatabaseConfig}" -c 'SELECT COUNT(CONTEXT) FROM Alert.Config_Contexts;' | grep -q '2';
    then
        logIt "Alert postgres database is initialized."
    else
        logIt "Preparing the old Alert database to be upgraded to 6.0.0..."
        if [ -f "${ALERT_DATA_DIR}/alertdb.mv.db" ];
        then
            logIt "A previous database existed."
            liquibaseChangelockReset
            logIt "Clearing old checksums for offline upgrade..."
            "${JAVA_HOME}/bin/java" -cp "${ALERT_TAR_HOME}/lib/liquibase/*" \
            liquibase.integration.commandline.Main \
            --url="jdbc:h2:file:${alertDatabaseDir}" \
            --username="sa" \
            --password="" \
            --driver="org.h2.Driver" \
            --changeLogFile="${upgradeResourcesDir}/changelog-master.xml" \
            clearCheckSums

            logIt "Upgrading old database to 5.3.0 so that it can be properly exported..."
            "${JAVA_HOME}/bin/java" -cp "${ALERT_TAR_HOME}/lib/liquibase/*" \
            liquibase.integration.commandline.Main \
            --url="jdbc:h2:file:${alertDatabaseDir}" \
            --username="sa" \
            --password="" \
            --driver="org.h2.Driver" \
            --changeLogFile="${upgradeResourcesDir}/changelog-master.xml" \
            update

            logIt "Creating temp directory for data migration..."
            mkdir -m 766 ${ALERT_DATA_DIR}/temp

            logIt "Exporting data from old database..."
            "${JAVA_HOME}/bin/java" -cp "${ALERT_TAR_HOME}/lib/liquibase/*" \
            org.h2.tools.RunScript \
            -url "jdbc:h2:${alertDatabaseDir}" \
            -user "sa" \
            -password "" \
            -driver "org.h2.Driver" \
            -script ${upgradeResourcesDir}/export_h2_tables.sql

            chmod 766 ${ALERT_DATA_DIR}/temp/*

            logIt "Importing data from old database into new database..."
            psql "${alertDatabaseConfig}" -f ${upgradeResourcesDir}/import_postgres_tables.sql
        else
            logIt "No previous database existed."
        fi
    fi
}

createPostgresExtensions() {
  logIt "Creating required postgres extensions."
  psql "${alertDatabaseAdminConfig}" -f ${upgradeResourcesDir}/create_extension.sql
}

setLocalVariableFromFileContents() {
  filename="${1}"
  localVariableName="${2}"
  if [ -s "${filename}" ];
  then
    logIt "${localVariableName} set with contents of ${filename}"
    eval "${localVariableName}=$(cat "${filename}" | xargs echo)"
  fi
  unset filename localVariableName
}

setGlobalVariableFromFileContents() {
  filename="${1}"
  globalVariableName="${2}"
  if [ -s "${filename}" ];
  then
    logIt "${globalVariableName} set with contents of ${filename}"
    eval "export ${globalVariableName}=$(cat "${filename}" | xargs echo)"
  fi
  unset filename globalVariableName
}

setVariablesFromFilePath() {
  filename="${1}"
  localVariableName="${2}"
  globalVariableName="${3}"
  if [ -s "${filename}" ];
  then
    logIt "${globalVariableName} variables set from ${filename}"
    eval "${localVariableName}=${filename}"
    eval "export ${globalVariableName}=${filename}"
  fi
  unset filename localVariableName globalVariableName
}

setOverrideVariables() {
    setLocalVariableFromFileContents "${dockerSecretDir}/ALERT_TRUST_STORE_PASSWORD" truststorePassword
    setLocalVariableFromFileContents "${dockerSecretDir}/ALERT_KEY_STORE_PASSWORD" keystorePassword

    setGlobalVariableFromFileContents "${dockerSecretDir}/ALERT_RABBITMQ_USER" ALERT_RABBITMQ_USER
    setGlobalVariableFromFileContents "${dockerSecretDir}/ALERT_RABBITMQ_PASSWORD" ALERT_RABBITMQ_PASSWORD

    setLocalVariableFromFileContents "${dockerSecretDir}/ALERT_DB_USERNAME" alertDatabaseUser
    setGlobalVariableFromFileContents "${dockerSecretDir}/ALERT_DB_USERNAME" ALERT_DB_USERNAME
    setLocalVariableFromFileContents "${dockerSecretDir}/ALERT_DB_USERNAME" alertDatabaseAdminUser
    setGlobalVariableFromFileContents "${dockerSecretDir}/ALERT_DB_USERNAME" ALERT_DB_ADMIN_USERNAME
    setLocalVariableFromFileContents "${dockerSecretDir}/ALERT_DB_ADMIN_USERNAME" alertDatabaseAdminUser
    setGlobalVariableFromFileContents "${dockerSecretDir}/ALERT_DB_ADMIN_USERNAME" ALERT_DB_ADMIN_USERNAME

    setLocalVariableFromFileContents "${dockerSecretDir}/ALERT_DB_PASSWORD" alertDatabasePassword
    setGlobalVariableFromFileContents "${dockerSecretDir}/ALERT_DB_PASSWORD" ALERT_DB_PASSWORD
    setLocalVariableFromFileContents "${dockerSecretDir}/ALERT_DB_PASSWORD" alertDatabaseAdminPassword
    setGlobalVariableFromFileContents "${dockerSecretDir}/ALERT_DB_PASSWORD" ALERT_DB_ADMIN_PASSWORD
    setLocalVariableFromFileContents "${dockerSecretDir}/ALERT_DB_ADMIN_PASSWORD" alertDatabaseAdminPassword
    setGlobalVariableFromFileContents "${dockerSecretDir}/ALERT_DB_ADMIN_PASSWORD" ALERT_DB_ADMIN_PASSWORD

    setVariablesFromFilePath "${dockerSecretDir}/ALERT_DB_SSL_KEY_PATH" alertDatabaseSslKey ALERT_DB_SSL_KEY_PATH
    setVariablesFromFilePath "${dockerSecretDir}/ALERT_DB_SSL_CERT_PATH" alertDatabaseSslCert ALERT_DB_SSL_CERT_PATH
    setVariablesFromFilePath "${dockerSecretDir}/ALERT_DB_SSL_ROOT_CERT_PATH" alertDatabaseSslRootCert ALERT_DB_SSL_ROOT_CERT_PATH
}

[ -z "${ALERT_HOSTNAME}" ] && logIt "Alert Host: [$alertHostName]. Wrong host name? Restart the container with the right host name configured in blackduck-alert.env"

setOverrideVariables

alertDatabaseAdminConfig="host=$alertDatabaseHost port=$alertDatabasePort dbname=$alertDatabaseName user=$alertDatabaseAdminUser password=$alertDatabaseAdminPassword sslmode=$alertDatabaseSslMode sslkey=$alertDatabaseSslKey sslcert=$alertDatabaseSslCert sslrootcert=$alertDatabaseSslRootCert"
alertDatabaseConfig="host=$alertDatabaseHost port=$alertDatabasePort dbname=$alertDatabaseName user=$alertDatabaseUser password=$alertDatabasePassword sslmode=$alertDatabaseSslMode sslkey=$alertDatabaseSslKey sslcert=$alertDatabaseSslCert sslrootcert=$alertDatabaseSslRootCert"

logIt "Alert max heap size: ${ALERT_MAX_HEAP_SIZE}"
logIt "Certificate authority host: $targetCAHost"
logIt "Certificate authority port: $targetCAPort"

if [ ! -f "${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh" ];
then
  checkStatus 2 "File does not exist: ${CERTIFICATE_MANAGER_DIR}/certificate-manager.sh"
fi

validatePostgresConnection
createCertificateStoreDirectory
manageCertificate
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
    export JAVA_OPTS="$JAVA_OPTS -Xmx${ALERT_MAX_HEAP_SIZE} -Djavax.net.ssl.trustStore=$truststoreFile"
fi

logIt "Launching exec :: $@"
exec "$@"
