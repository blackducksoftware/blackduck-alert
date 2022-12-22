package com.synopsys.integration.alert.database.authentication.saml;

import com.synopsys.integration.alert.database.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "alert", name = "configuration_saml")
public class AuthenticationSAMLConfigurationEntity extends BaseEntity {
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
    @Column(name = "metadata_path")
    private String metadataPath;
    @Column(name = "entity_id")
    private String entityId;
    @Column(name = "entity_base_url")
    private String entityBaseUrl;
    @Column(name = "require_assertions_signed")
    private Boolean requireAssertionsSigned;
    @Column(name = "role_attribute_mapping")
    private String roleAttributeMapping;

    public AuthenticationSAMLConfigurationEntity() {
    }

    public AuthenticationSAMLConfigurationEntity(
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime lastUpdated,
        Boolean enabled,
        Boolean forceAuth,
        String metadataUrl,
        String metadataPath,
        String entityId,
        String entityBaseUrl,
        Boolean requireAssertionsSigned,
        String roleAttributeMapping
    ) {
        this.name = name;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.enabled = enabled;
        this.forceAuth = forceAuth;
        this.metadataUrl = metadataUrl;
        this.metadataPath = metadataPath;
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

    public String getMetadataPath() {
        return metadataPath;
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
