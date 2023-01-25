package com.synopsys.integration.alert.authentication.saml.model;

import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;

import java.util.Optional;

public class SAMLConfigModel extends ConfigWithMetadata implements Obfuscated<SAMLConfigModel> {
    private Boolean enabled;
    private Boolean forceAuth;
    private String metadataUrl;
    private String metadataFilePath;
    private String entityId;
    private String entityBaseUrl;
    private Boolean wantAssertionsSigned;
    private String roleAttributeMapping;
    private String encryptionCertFilePath;
    private String signingCertFilePath;
    private String verificationCertFilePath;
    private SAMLMetadataMode metadataMode;

    public SAMLConfigModel() {
        // For serialization
    }

    // Required
    public SAMLConfigModel(String id, String entityId, String entityBaseUrl) {
        super(id, AlertRestConstants.DEFAULT_CONFIGURATION_NAME);

        this.entityId = entityId;
        this.entityBaseUrl = entityBaseUrl;
    }

    public SAMLConfigModel(
        String id,
        String createdAt,
        String lastUpdated,
        Boolean enabled,
        Boolean forceAuth,
        String metadataUrl,
        String metadataFilePath,
        String entityId,
        String entityBaseUrl,
        Boolean wantAssertionsSigned,
        String roleAttributeMapping,
        String encryptionCertFilePath,
        String signingCertFilePath,
        String verificationCertFilePath,
        SAMLMetadataMode metadataMode
    ) {
        this(id, entityId, entityBaseUrl);
        this.enabled = enabled;
        this.forceAuth = forceAuth;
        this.metadataUrl = metadataUrl;
        this.metadataFilePath = metadataFilePath;
        this.wantAssertionsSigned = wantAssertionsSigned;
        this.roleAttributeMapping = roleAttributeMapping;
        this.encryptionCertFilePath = encryptionCertFilePath;
        this.signingCertFilePath = signingCertFilePath;
        this.verificationCertFilePath = verificationCertFilePath;
        this.metadataMode = metadataMode;

        setCreatedAt(createdAt);
        setLastUpdated(lastUpdated);
    }

    @Override
    public SAMLConfigModel obfuscate() {
        return new SAMLConfigModel(
            getId(),
            getCreatedAt(),
            getLastUpdated(),
            enabled,
            forceAuth,
            metadataUrl,
            metadataFilePath,
            entityId,
            entityBaseUrl,
            wantAssertionsSigned,
            roleAttributeMapping,
            encryptionCertFilePath,
            signingCertFilePath,
            verificationCertFilePath,
            metadataMode
        );
    }

    // Getters
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

    public String getEntityId() {
        return entityId;
    }

    public String getEntityBaseUrl() {
        return entityBaseUrl;
    }

    public Optional<Boolean> getWantAssertionsSigned() {
        return Optional.ofNullable(wantAssertionsSigned);
    }

    public Optional<String> getRoleAttributeMapping() {
        return Optional.ofNullable(roleAttributeMapping);
    }

    public Optional<String> getEncryptionCertFilePath() {
        return Optional.ofNullable(encryptionCertFilePath);
    }

    public Optional<String> getSigningCertFilePath() {
        return Optional.ofNullable(signingCertFilePath);
    }

    public Optional<String> getVerificationCertFilePath() {
        return Optional.ofNullable(verificationCertFilePath);
    }

    public Optional<SAMLMetadataMode> getMetadataMode() {
        return Optional.ofNullable(metadataMode);
    }

    // Setters
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setForceAuth(Boolean forceAuth) {
        this.forceAuth = forceAuth;
    }

    public void setMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setEntityBaseUrl(String entityBaseUrl) {
        this.entityBaseUrl = entityBaseUrl;
    }

    public void setRoleAttributeMapping(String roleAttributeMapping) {
        this.roleAttributeMapping = roleAttributeMapping;
    }
}
