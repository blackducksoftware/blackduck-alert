package com.synopsys.integration.alert.component.saml;

import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;

import java.util.Optional;

public class SAMLGlobalConfigModel extends ConfigWithMetadata implements Obfuscated<SAMLGlobalConfigModel> {
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
    public SAMLGlobalConfigModel (String id) {
        super(id, AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    public SAMLGlobalConfigModel (
        String id,
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
        this(id);
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

    @Override
    public SAMLGlobalConfigModel obfuscate() {
        return new SAMLGlobalConfigModel(
            getId(),
            getCreatedAt(),
            getLastUpdated(),
            enabled,
            forceAuth,
            metadataUrl,
            metadataFilePath,
            entityId,
            entityBaseUrl,
            requireAssertionsSigned,
            roleAttributeMapping
        );
    }

    public Optional<Boolean> getEnabled() {
        return Optional.ofNullable(enabled);
    }

    public Optional<Boolean> getForceAuth() {
        return Optional.ofNullable(forceAuth);
    }

    public Optional<String> getMetadataUrl() {
        return Optional.ofNullable(metadataUrl);
    }

    public Optional<String> getMetadataFilePath() {
        return Optional.ofNullable(metadataFilePath);
    }

    public Optional<String> getEntityId() {
        return Optional.ofNullable(entityId);
    }

    public Optional<String> getEntityBaseUrl() {
        return Optional.ofNullable(entityBaseUrl);
    }

    public Optional<Boolean> getRequireAssertionsSigned() {
        return Optional.ofNullable(requireAssertionsSigned);
    }

    public Optional<String> getRoleAttributeMapping() {
        return Optional.ofNullable(roleAttributeMapping);
    }
}
