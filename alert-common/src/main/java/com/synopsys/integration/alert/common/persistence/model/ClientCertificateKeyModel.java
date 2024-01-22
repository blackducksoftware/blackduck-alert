package com.synopsys.integration.alert.common.persistence.model;

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class ClientCertificateKeyModel extends AlertSerializableModel implements Obfuscated<ClientCertificateKeyModel> {
    private UUID id;

    private String name;

    private String password;

    private Boolean isPasswordSet;

    private String keyContent;

    private String lastUpdated;

    public ClientCertificateKeyModel(UUID id, String name, String password, Boolean isPasswordSet, String keyContent, String lastUpdated) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.isPasswordSet = isPasswordSet;
        this.keyContent = keyContent;
        this.lastUpdated = lastUpdated;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public Boolean getIsPasswordSet() {
        return isPasswordSet;
    }

    public String getKeyContent() {
        return keyContent;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public ClientCertificateKeyModel obfuscate() {
        return new ClientCertificateKeyModel(
                id,
                name,
                null,
                StringUtils.isNotBlank(password),
                keyContent,
                lastUpdated);
    }
}
