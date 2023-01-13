package com.synopsys.integration.alert.authentication.saml.database.configuration;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.database.BaseEntity;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "alert", name = "configuration_saml")
public class SAMLConfigurationEntity extends BaseEntity {
    @Id
    @Column(name = "configuration_id")
    private UUID configurationId;
    @Column(name = "name")
    private String name;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;
    @Column(name = "enabled")
    private Boolean enabled;
    @Column(name = "force_auth")
    private Boolean forceAuth;
    @Column(name = "metadata_url")
    private String metadataUrl;
    @Column(name = "metadata_file_path")
    private String metadataFilePath;
    @Column(name = "entity_id")
    private String entityId;
    @Column(name = "entity_base_url")
    private String entityBaseUrl;
    @Column(name = "require_assertions_signed")
    private Boolean requireAssertionsSigned;
    @Column(name = "role_attribute_mapping")
    private String roleAttributeMapping;

    public SAMLConfigurationEntity() {
    }

    public SAMLConfigurationEntity(
        UUID configurationId,
        OffsetDateTime createdAt,
        OffsetDateTime lastUpdated,
        Boolean enabled,
        Boolean forceAuth,
        String metadataUrl,
        String metadataFilePath,
        String entityId,
        String entityBaseUrl,
        Boolean requireAssertionsSigned,
        String roleAttributeMapping
    ) {
        this.configurationId = configurationId;
        this.name = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.enabled = enabled;
        this.forceAuth = forceAuth;
        this.metadataUrl = metadataUrl;
        this.metadataFilePath = metadataFilePath;
        this.entityId = entityId;
        this.entityBaseUrl = entityBaseUrl;
        this.requireAssertionsSigned = requireAssertionsSigned;
        this.roleAttributeMapping = roleAttributeMapping;
    }

    public UUID getConfigurationId() {
        return configurationId;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean getForceAuth() {
        return forceAuth;
    }

    public String getMetadataUrl() {
        return metadataUrl;
    }

    public String getMetadataFilePath() {
        return metadataFilePath;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getEntityBaseUrl() {
        return entityBaseUrl;
    }

    public Boolean getRequireAssertionsSigned() {
        return requireAssertionsSigned;
    }

    public String getRoleAttributeMapping() {
        return roleAttributeMapping;
    }
}
