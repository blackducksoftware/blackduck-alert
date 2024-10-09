package com.blackduck.integration.alert.channel.email.database.configuration.properties;

import java.io.Serializable;
import java.util.UUID;

public class EmailConfigurationPropertyPK implements Serializable {
    private static final long serialVersionUID = -6305320156508472382L;
    private UUID configurationId;
    private String propertyKey;

    public EmailConfigurationPropertyPK() {
    }

    public EmailConfigurationPropertyPK(UUID configurationId, String propertyKey) {
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
