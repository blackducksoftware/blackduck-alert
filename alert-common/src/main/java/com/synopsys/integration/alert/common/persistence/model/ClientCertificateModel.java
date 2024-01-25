package com.synopsys.integration.alert.common.persistence.model;

import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class ClientCertificateModel extends AlertSerializableModel {
    private UUID id;
    private UUID privateKeyId;
    private String certificateContent;
    private String lastUpdated;

    public ClientCertificateModel(UUID id, UUID privateKeyId, String certificateContent, String lastUpdated) {
        this.id = id;
        this.privateKeyId = privateKeyId;
        this.certificateContent = certificateContent;
        this.lastUpdated = lastUpdated;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPrivateKeyId() {
        return privateKeyId;
    }

    public String getCertificateContent() {
        return certificateContent;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }
}
