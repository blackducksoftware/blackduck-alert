package com.synopsys.integration.alert.common.persistence.model;

import java.io.Serial;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class ClientCertificateModel extends AlertSerializableModel implements Obfuscated<ClientCertificateModel> {
    @Serial
    private static final long serialVersionUID = -2254327445871324025L;
    private String keyPassword;
    private String keyContent;
    private String certificateContent;

    public ClientCertificateModel() {
        // For serialization
    }

    public ClientCertificateModel(String keyPassword, String keyContent, String certificateContent) {
        this.keyPassword = keyPassword;
        this.keyContent = keyContent;
        this.certificateContent = certificateContent;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public String getKeyContent() {
        return keyContent;
    }

    public String getCertificateContent() {
        return certificateContent;
    }

    @Override
    public ClientCertificateModel obfuscate() {
        return new ClientCertificateModel(null, null, null);
    }
}
