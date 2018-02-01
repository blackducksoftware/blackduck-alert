#!/bin/sh
set -e

verifyEnvironment() {
  # Verify JRE is present.
  if [ -n "$JAVA_HOME" ]; then
    if [ -d "$JAVA_HOME" ]; then
      if [ ! -f "$JAVA_HOME/lib/security/cacerts" ]; then
        echo "ERROR: $JAVA_HOME/lib/security/cacerts is not a file or does not exist."
      fi
    else
      echo "ERROR: $JAVA_HOME is not a directory or does not exist."
      exit 1
    fi
  else
    echo "ERROR: JAVA_HOME is not defined."
    exit 1
  fi
}

importCertificate(){
	echo "Attempting to import Hub Certificate"
	echo $PUBLIC_HUB_WEBSERVER_HOST
	echo $PUBLIC_HUB_WEBSERVER_PORT

	# In case of email-extension container restart
	if keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit -alias publichubwebserver
	then
	    keytool -delete -alias publichubwebserver -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit
		echo "Removing the existing certificate after container restart"
	fi

	if keytool -printcert -rfc -sslserver "$PUBLIC_HUB_WEBSERVER_HOST:$PUBLIC_HUB_WEBSERVER_PORT" -v | keytool -importcert -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit -alias publichubwebserver -noprompt
	then
		echo "Completed importing Hub Certificate"
	else
		echo "Unable to add the certificate. Please try to import the certificate manually."
	fi
}

# Bootstrap will optionally configure the config volume if it hasnt been configured yet.
# After that we verify, import certs, and then launch the webserver.

verifyEnvironment

if [ "$ALERT_IMPORT_CERT" == "false" ]
then
    echo "Skipping import of Hub Certificate"
else
    importCertificate
fi
exec "$@"
