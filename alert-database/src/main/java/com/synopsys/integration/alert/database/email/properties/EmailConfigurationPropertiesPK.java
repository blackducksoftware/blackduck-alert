package com.synopsys.integration.alert.database.email.properties;

import java.io.Serializable;
import java.util.UUID;

public class EmailConfigurationPropertiesPK implements Serializable {

    private UUID configurationId;
    private String propertyKey;

    public EmailConfigurationPropertiesPK() {
    }

    public EmailConfigurationPropertiesPK(UUID configurationId, String propertyKey) {
        this.configurationId = configurationId;
        this.propertyKey = propertyKey;
    }

    public UUID getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(UUID configurationId) {
        this.configurationId = configurationId;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }
}
