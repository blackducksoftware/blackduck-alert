#!/bin/sh

certificateManagerDir=/opt/blackduck/alert/bin
securityDir=/opt/blackduck/alert/security
alertHome=/opt/blackduck/alert
alertConfigHome=$alertHome/alert-config

serverCertName=$APPLICATION_NAME-server

dockerSecretDir=${RUN_SECRETS_DIR:-/run/secrets}
keyStoreFile=$APPLICATION_NAME.keystore
keystoreFilePath=$securityDir/$keyStoreFile
keystorePassword="${ALERT_KEY_STORE_PASSWORD:-changeit}"
truststoreFile=$securityDir/$APPLICATION_NAME.truststore
truststorePassword="${ALERT_TRUST_STORE_PASSWORD:-changeit}"
truststoreType="${ALERT_TRUST_STORE_TYPE:-JKS}"

publicWebserverHost="${ALERT_HOSTNAME:-localhost}"
targetCAHost="${HUB_CFSSL_HOST:-cfssl}"
targetCAPort="${HUB_CFSSL_PORT:-8888}"
targetWebAppHost="${HUB_WEBAPP_HOST:-alert}"

[ -z "$ALERT_HOSTNAME" ] && echo "Public Webserver Host: [$publicWebserverHost]. Wrong host name? Restart the container with the right host name configured in blackduck-alert.env"

if [ -e $dockerSecretDir/ALERT_TRUST_STORE_PASSWORD ];
then
  echo "Trust Store secret set; using value from secret."
  truststorePassword=$(cat $dockerSecretDir/ALERT_TRUST_STORE_PASSWORD)
fi

if [ -e $dockerSecretDir/ALERT_KEY_STORE_PASSWORD ];
then
  echo "Key Store secret set; using value from secret."
  keystorePassword=$(cat $dockerSecretDir/ALERT_KEY_STORE_PASSWORD)
fi

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
        --san $publicWebserverHost \
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
# After that we verify, import certs, and then launch the webserver.

importBlackDuckWebServerCertificate(){
    if [ "$ALERT_IMPORT_CERT" == "false" ];
    then
        echo "Skipping import of BlackDuck Certificate"
    else
      if [ -z "$PUBLIC_HUB_WEBSERVER_HOST" ];
      then
        echo "PUBLIC_HUB_WEBSERVER_HOST and/or PUBLIC_HUB_WEBSERVER_PORT not set.  Skipping import of BlackDuck Certificate"
      else
        echo "Attempting to import BlackDuck Certificate"
        echo $PUBLIC_HUB_WEBSERVER_HOST
        echo $PUBLIC_HUB_WEBSERVER_PORT

        # In case of alert container restart
        if keytool -list -keystore "$truststoreFile" -storepass $truststorePassword -alias "$PUBLIC_HUB_WEBSERVER_HOST"
        then
            keytool -delete -alias "$PUBLIC_HUB_WEBSERVER_HOST" -keystore "$truststoreFile" -storepass $truststorePassword
          echo "Removing the existing BlackDuck certificate after container restart"
        fi

        if [ -z "$PUBLIC_HUB_WEBSERVER_PORT"];
        then
          if keytool -printcert -rfc -sslserver "$PUBLIC_HUB_WEBSERVER_HOST" -v | keytool -importcert -keystore "$truststoreFile" -storepass $truststorePassword -alias "$PUBLIC_HUB_WEBSERVER_HOST" -noprompt
          then
            echo "Completed importing BlackDuck Certificate"
          else
            echo "Unable to add the BlackDuck certificate. Please try to import the certificate manually."
          fi
        else
          if keytool -printcert -rfc -sslserver "$PUBLIC_HUB_WEBSERVER_HOST:$PUBLIC_HUB_WEBSERVER_PORT" -v | keytool -importcert -keystore "$truststoreFile" -storepass $truststorePassword -alias "$PUBLIC_HUB_WEBSERVER_HOST" -noprompt
          then
            echo "Completed importing BlackDuck certificate"
          else
            echo "Unable to add the BlackDuck certificate. Please try to import the certificate manually."
          fi
        fi
    	fi
    fi
}

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

createDataBackUp(){
    if [ -d $alertConfigHome/data ];
    then
      echo "Creating a backup of the data directory: $alertConfigHome/data"
      if [ -f "$alertConfigHome/data/backup.zip" ];
        then
        rm -f "$alertConfigHome/data/backup.zip"
      fi
      cd "$alertConfigHome"
      zip "$alertConfigHome/backup.zip" -r "data"
      if [ -f "$alertConfigHome/backup.zip" ];
        then
          echo "Created a backup of the data directory: $alertConfigHome/backup.zip"
          echo "Moving backup file to: $alertConfigHome/data"
          mv "$alertConfigHome/backup.zip" "$alertConfigHome/data"
        else
          echo "Cannot create the backup."
          echo "Cannot continue; stopping in 10 seconds..."
          sleep 10
          exit 1;
      fi
    fi
}

checkVolumeDirectories() {
  echo "Checking volume directory: $alertConfigHome"
  if [ -d $alertConfigHome ];
  then
    echo "$alertConfigHome exists"
    echo "Validating write access..."

    if [ -d $alertConfigHome/data ];
    then
      echo "$alertConfigHome/data exists"
    else
      mkdir $alertConfigHome/data
    fi

    testFile=$alertConfigHome/data/volumeAccessTest.txt
    touch $testFile

    if [ -f $testFile ];
    then
      echo "Validated write access to directory: $alertConfigHome"
      rm $testFile
    else
      echo "Cannot write to volume directory: $alertConfigHome"
      echo "Cannot continue; stopping in 10 seconds..."
      sleep 10
      exit 1;
    fi
  fi
}

checkVolumeDirectories

if [ ! -f "$certificateManagerDir/certificate-manager.sh" ];
then
  echo "ERROR: certificate management script is not present."
  sleep 10
  exit 1;
else
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
  importBlackDuckWebServerCertificate
  importDockerHubServerCertificate
  createDataBackUp

  if [ -f "$truststoreFile" ];
  then
      JAVA_OPTS="$JAVA_OPTS -Xmx$ALERT_MAX_HEAP_SIZE -Djavax.net.ssl.trustStore=$truststoreFile"
      export JAVA_OPTS
  fi
fi

exec "$@"
