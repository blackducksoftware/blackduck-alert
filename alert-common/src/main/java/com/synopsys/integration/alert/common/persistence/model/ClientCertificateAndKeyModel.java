package com.synopsys.integration.alert.common.persistence.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class ClientCertificateAndKeyModel extends AlertSerializableModel implements Obfuscated<ClientCertificateAndKeyModel> {
    private String keyPassword;
    private String keyContent;
    private String certificateContent;

    public ClientCertificateAndKeyModel(String keyPassword, String keyContent, String certificateContent) {
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
    public ClientCertificateAndKeyModel obfuscate() {
        return new ClientCertificateAndKeyModel(null, null, null);
    }
}
