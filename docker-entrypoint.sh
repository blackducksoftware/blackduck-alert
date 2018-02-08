#!/bin/sh
set -e

certificateManagerDir=/opt/blackduck/security/bin
securityDir=/opt/blackduck/security
dataDir=/opt/blackduck/alert/alert-config/data

serverCertName=$APPLICATION_NAME-server

dockerSecretDir=${RUN_SECRETS_DIR:-/run/secrets}
keyStoreFile=$APPLICATION_NAME.keystore
keystorePath=$securityDir/$keyStoreFile
truststoreFile=$securityDir/$APPLICATION_NAME.truststore

publicWebserverHost="${PUBLIC_HUB_WEBSERVER_HOST:-localhost}"
targetCAHost="${HUB_CFSSL_HOST:-cfssl}"
targetCAPort="${HUB_CFSSL_PORT:-8888}"
targetWebAppHost="${HUB_WEBAPP_HOST:-alert}"

[ -z "$PUBLIC_HUB_WEBSERVER_HOST" ] && echo "Public Webserver Host: [$publicWebserverHost]. Wrong host name? Restart the container with the right host name configured in hub-webserver.env"

echo "Certificate authority host: $targetCAHost"
echo "Certificate authority port: $targetCAPort"

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
        --hostName $publicWebserverHost
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

    # Create the keystore with given private key and certificate.
    echo "Attempting to create keystore."
    $certificateManagerDir/certificate-manager.sh keystore \
                                             --outputDirectory $securityDir \
                                             --outputFile $keyStoreFile \
                                             --password changeit \
                                             --keyAlias $APPLICATION_NAME \
                                             --key $securityDir/$serverCertName.key \
                                             --cert $securityDir/$serverCertName.crt
    exitCode=$?
    if [ $exitCode -eq 0 ];
    then
        chmod 644 $keystorePath
    else
        echo "Unable to create keystore (Code: $exitCode)."
        exit $exitCode
    fi
}

createTruststore() {
    echo "Attempting to copy Java cacerts to create truststore."
    $certificateManagerDir/certificate-manager.sh truststore --outputDirectory $securityDir --outputFile $APPLICATION_NAME.truststore
    exitCode=$?
    if [ ! $exitCode -eq 0 ]; then
        echo "Unable to create truststore (Code: $exitCode)."
        exit $exitCode
    fi
}

trustProxyCertificate() {
    proxyCertificate="$dockerSecretDir/HUB_PROXY_CERT_FILE"

    if [ ! -f "$dockerSecretDir/HUB_PROXY_CERT_FILE" ]; then
        echo "WARNING: Proxy certificate file is not found in secret. Skipping Proxy Certificate Import."
    else
        $certificateManagerDir/certificate-manager.sh trust-java-cert \
                                --store $truststoreFile \
                                --password changeit \
                                --cert $proxyCertificate \
                                --certAlias proxycert
        exitCode=$?
        if [ $exitCode -eq 0 ]; then
            echo "Successfully imported proxy certificate into Java truststore."
        else
            echo "Unable to import proxy certificate into Java truststore (Code: $exitCode)."
        fi
    fi
}

trustRootCertificate() {
    $certificateManagerDir/certificate-manager.sh trust-java-cert \
                        --store $truststoreFile \
                        --password changeit \
                        --cert $securityDir/root.crt \
                        --certAlias hub-root

    exitCode=$?
    if [ $exitCode -eq 0 ]; then
        echo "Successfully imported Hub root certificate into Java truststore."
    else
        echo "Unable to import Hub root certificate into Java truststore (Code: $exitCode)."
        exit $exitCode
    fi
}

# Bootstrap will optionally configure the config volume if it hasnt been configured yet.
# After that we verify, import certs, and then launch the webserver.

importWebServerCertificate(){
    if [ "$ALERT_IMPORT_CERT" == "false" ]
    then
        echo "Skipping import of Hub Certificate"
    else
    	echo "Attempting to import Hub Certificate"
    	echo $PUBLIC_HUB_WEBSERVER_HOST
    	echo $PUBLIC_HUB_WEBSERVER_PORT

    	# In case of email-extension container restart
    	if keytool -list -keystore "$truststoreFile" -storepass changeit -alias publichubwebserver
    	then
    	    keytool -delete -alias publichubwebserver -keystore "$truststoreFile" -storepass changeit
    		echo "Removing the existing certificate after container restart"
    	fi

    	if keytool -printcert -rfc -sslserver "$PUBLIC_HUB_WEBSERVER_HOST:$PUBLIC_HUB_WEBSERVER_PORT" -v | keytool -importcert -keystore "$truststoreFile" -storepass changeit -alias publichubwebserver -noprompt
    	then
    		echo "Completed importing Hub Certificate"
    	else
    		echo "Unable to add the certificate. Please try to import the certificate manually."
    	fi
    fi
}

if [ ! -f "$certificateManagerDir/certificate-manager.sh" ];
then
  echo "ERROR: certificate management script is not present."
  exit 1;
else
  manageSelfSignedServerCertificate
  createTruststore
  trustRootCertificate
  trustProxyCertificate
  importWebServerCertificate

  if [ -f "$truststoreFile" ]; then
      JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=$truststoreFile"
      export JAVA_OPTS
  fi
fi

exec "$@"
