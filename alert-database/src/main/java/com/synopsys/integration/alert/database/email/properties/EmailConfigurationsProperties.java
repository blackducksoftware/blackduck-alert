package com.synopsys.integration.alert.database.email.properties;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@IdClass(EmailConfigurationPropertiesPK.class)
@Table(schema = "alert", name = "configuration_email_properties")
public class EmailConfigurationsProperties extends BaseEntity {
    @Id
    @Column(name = "configuration_id")
    private final UUID configurationId;
    @Column(name = "property_key")
    private final String propertyKey;
    @Column(name = "property_value")
    private final String propertyValue;

    public EmailConfigurationsProperties(UUID configurationId, String propertyKey, String propertyValue) {
        this.configurationId = configurationId;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
    }

    public UUID getConfigurationId() {
        return configurationId;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getPropertyValue() {
        return propertyValue;
    }
}
