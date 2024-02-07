package com.synopsys.integration.alert.component.certificates;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ssl.pem.PemSslStoreBundle;
import org.springframework.boot.ssl.pem.PemSslStoreDetails;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateKeyModel;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;

@Component
public class AlertClientCertificateManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PemSslStoreBundle clientSslStoreBundle;
    private String clientKeyPassword;

    public synchronized void importCertificate(ClientCertificateModel clientCertificateModel, ClientCertificateKeyModel clientCertificateKeyModel)
        throws AlertException {
        logger.debug("Importing certificate into key store.");
        validateClientCertificateHasValues(clientCertificateModel);
        validateCertificateKeyHasValues(clientCertificateKeyModel);
        clientKeyPassword = clientCertificateKeyModel.getPassword()
            .orElseThrow(() -> new AlertException("Missing private key password for client certificate"));
        PemSslStoreDetails keyStoreDetails = PemSslStoreDetails.forCertificate(clientCertificateModel.getCertificateContent())
            .withPrivateKey(clientCertificateKeyModel.getKeyContent())
            .withPrivateKeyPassword(clientKeyPassword);
        PemSslStoreDetails trustStoreDetails = PemSslStoreDetails.forCertificate(null);
        clientSslStoreBundle = new PemSslStoreBundle(keyStoreDetails, trustStoreDetails, AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS);
    }

    public synchronized void removeCertificate() throws AlertException {
        logger.debug("Removing certificate from key store.");
        Optional<KeyStore> clientKeystore = getClientKeyStore();
        if (clientKeystore.isPresent()) {
            try {
                clientKeystore.get().deleteEntry(AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS);
            } catch (KeyStoreException e) {
                // this shouldn't happen because the delete will throw an exception if the keystore isn't initialized but the import would fail if the certificate or key can't be read.
                // This is just defensive programming.
                throw new AlertException(String.format("Error removing client certificate entry for alias: %s", AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS));
            }
        }
        // clean up the reference
        clientSslStoreBundle = null;
        clientKeyPassword = null;
    }

    public boolean containsClientCertificate() {
        return null != clientSslStoreBundle && null != clientKeyPassword;
    }

    public Optional<KeyStore> getClientKeyStore() {
        return Optional.ofNullable(clientSslStoreBundle)
            .map(PemSslStoreBundle::getKeyStore);
    }

    public Optional<String> getClientKeyPassword() {
        return Optional.ofNullable(clientKeyPassword);
    }

    private void validateClientCertificateHasValues(ClientCertificateModel clientCertificateModel) throws AlertException {
        if (null == clientCertificateModel) {
            throw new AlertException("The custom certificate cannot be null.");
        }

        if (StringUtils.isBlank(clientCertificateModel.getCertificateContent())) {
            throw new AlertException("The certificate content cannot be blank.");
        }
    }

    private void validateCertificateKeyHasValues(ClientCertificateKeyModel clientCertificateKeyModel) throws AlertException {
        if (null == clientCertificateKeyModel) {
            throw new AlertException("The custom certificate key cannot be null.");
        }

        if (StringUtils.isBlank(clientCertificateKeyModel.getKeyContent())) {
            throw new AlertException("The certificate key content cannot be blank.");
        }

        boolean passwordMissing = clientCertificateKeyModel.getPassword()
            .filter(StringUtils::isNotBlank)
            .isEmpty();

        if (passwordMissing) {
            throw new AlertException("The certificate key password cannot be blank.");
        }
    }
}
