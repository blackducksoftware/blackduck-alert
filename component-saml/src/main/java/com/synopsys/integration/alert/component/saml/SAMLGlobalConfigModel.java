package com.synopsys.integration.alert.component.saml;

import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;

import java.util.Optional;

public class SAMLGlobalConfigModel extends ConfigWithMetadata {
    private Boolean enabled;
    private Boolean forceAuth;
    private String metadataUrl;
    private String metadataFilePath;
    private String entityId;
    private String entityBaseUrl;
    private Boolean requireAssertionsSigned;
    private String roleAttributeMapping;

    public SAMLGlobalConfigModel () {
        // For serialization
    }

    // Required
    public SAMLGlobalConfigModel (String id, String name) {
        super(id, name);
    }

    public SAMLGlobalConfigModel (
        String id,
        String name,
        String createdAt,
        String lastUpdated,
        Boolean enabled,
        Boolean forceAuth,
        String metadataUrl,
        String metadataFilePath,
        String entityId,
        String entityBaseUrl,
        Boolean requireAssertionsSigned,
        String roleAttributeMapping
    ) {
        this(id, name);
        this.enabled = enabled;
        this.forceAuth = forceAuth;
        this.metadataUrl = metadataUrl;
        this.metadataFilePath = metadataFilePath;
        this.entityId = entityId;
        this.entityBaseUrl = entityBaseUrl;
        this.requireAssertionsSigned = requireAssertionsSigned;
        this.roleAttributeMapping = roleAttributeMapping;

        setCreatedAt(createdAt);
        setLastUpdated(lastUpdated);
    }

    public Optional<Boolean> getEnabled() { return Optional.ofNullable(enabled); }

    public Optional<Boolean> getForceAuth() { return Optional.ofNullable(forceAuth); }

    public Optional<String> getMetadataUrl() { return Optional.ofNullable(metadataUrl); }

    public Optional<String> getMetadataFilePath() { return Optional.ofNullable(metadataFilePath); }

    public Optional<String> getEntityId() { return Optional.ofNullable(entityId); }

    public Optional<String> getEntityBaseUrl() { return Optional.ofNullable(entityBaseUrl); }

    public Optional<Boolean> getRequireAssertionsSigned() { return Optional.ofNullable(requireAssertionsSigned); }

    public Optional<String> getRoleAttributeMapping() { return Optional.ofNullable(roleAttributeMapping); }
}
