#!/bin/sh
set -e

securityDir=/opt/blackduck/security

serverCertName=$APPLICATION_NAME-server

dockerSecretDir=${RUN_SECRETS_DIR:-/run/secrets}
truststoreFile=$securityDir/$APPLICATION_NAME.truststore

publicWebserverHost="${PUBLIC_HUB_WEBSERVER_HOST:-localhost}"
targetCAHost="${HUB_CFSSL_HOST:-cfssl}"
targetCAPort="${HUB_CFSSL_PORT:-8888}"
targetWebAppHost="${HUB_WEBAPP_HOST:-alert}"

[ -z "$PUBLIC_HUB_WEBSERVER_HOST" ] && echo "Public Webserver Host: [$publicWebserverHost]. Wrong host name? Restart the container with the right host name configured in hub-webserver.env"

echo "Certificate authority host: $targetCAHost"
echo "Certificate authority port: $targetCAPort"

manageSelfSignedServerCertificate() {
    echo "Attempting to generate $HUB_APPLICATION_NAME self-signed server certificate and key."
    $securityDir/bin/certificate-manager.sh server-cert \
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
}

createTruststore() {
    echo "Attempting to copy Java cacerts to create truststore."
    $securityDir/bin/certificate-manager.sh truststore --outputDirectory $securityDir --outputFile $APPLICATION_NAME.truststore
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
        $securityDir/bin/certificate-manager.sh trust-java-cert \
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
    $securityDir/bin/certificate-manager.sh trust-java-cert \
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

if [ ! -f "$securityDir/bin/certificate-manager.sh" ];
then
  echo "ERROR: certificate management script is not present."
  exit 1;
else
  manageSelfSignedServerCertificate
  createTruststore
  trustRootCertificate
  trustProxyCertificate
fi

exec "$@"
