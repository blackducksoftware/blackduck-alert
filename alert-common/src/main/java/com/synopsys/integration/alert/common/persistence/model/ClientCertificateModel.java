package com.synopsys.integration.alert.common.persistence.model;

import java.io.Serial;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.api.common.model.Obfuscated;

public class ClientCertificateModel extends AlertSerializableModel implements Obfuscated<ClientCertificateModel> {
    @Serial
    private static final long serialVersionUID = -2254327445871324025L;
    private String keyPassword;
    private String keyContent;
    private String clientCertificateContent;

    public ClientCertificateModel() {
        // For serialization
    }

    public ClientCertificateModel(String keyPassword, String keyContent, String clientCertificateContent) {
        this.keyPassword = keyPassword;
        this.keyContent = keyContent;
        this.clientCertificateContent = clientCertificateContent;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public String getKeyContent() {
        return keyContent;
    }

    public String getClientCertificateContent() {
        return clientCertificateContent;
    }

    @Override
    public ClientCertificateModel obfuscate() {
        return new ClientCertificateModel(null, null, null);
    }
}
