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
    private SAMLMetadataMode metadataMode;
    private String entityId;
    private String entityBaseUrl;
    private Boolean wantAssertionsSigned;
    private String roleAttributeMapping;
    private String encryptionCertFilePath;
    private String encryptionPrivateKeyFilePath;
    private String signingCertFilePath;
    private String signingPrivateKeyFilePath;
    private String verificationCertFilePath;

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
        SAMLMetadataMode metadataMode,
        String entityId,
        String entityBaseUrl,
        Boolean wantAssertionsSigned,
        String roleAttributeMapping,
        String encryptionCertFilePath,
        String encryptionPrivateKeyFilePath,
        String signingCertFilePath,
        String signingPrivateKeyFilePath,
        String verificationCertFilePath
    ) {
        this(id, entityId, entityBaseUrl);
        this.enabled = enabled;
        this.forceAuth = forceAuth;
        this.metadataUrl = metadataUrl;
        this.metadataFilePath = metadataFilePath;
        this.metadataMode = metadataMode;
        this.wantAssertionsSigned = wantAssertionsSigned;
        this.roleAttributeMapping = roleAttributeMapping;
        this.encryptionCertFilePath = encryptionCertFilePath;
        this.encryptionPrivateKeyFilePath = encryptionPrivateKeyFilePath;
        this.signingCertFilePath = signingCertFilePath;
        this.signingPrivateKeyFilePath = signingPrivateKeyFilePath;
        this.verificationCertFilePath = verificationCertFilePath;

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
            metadataMode,
            entityId,
            entityBaseUrl,
            wantAssertionsSigned,
            roleAttributeMapping,
            encryptionCertFilePath,
            encryptionPrivateKeyFilePath,
            signingCertFilePath,
            signingPrivateKeyFilePath,
            verificationCertFilePath
        );
    }

    // Getters
    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean getForceAuth() {
        return forceAuth;
    }

    public Optional<String> getMetadataUrl() {
        return Optional.ofNullable(metadataUrl);
    }

    public Optional<String> getMetadataFilePath() {
        return Optional.ofNullable(metadataFilePath);
    }

    public Optional<SAMLMetadataMode> getMetadataMode() {
        return Optional.ofNullable(metadataMode);
    }

    public String getEntityId() {
        return entityId;
    }

    public String getEntityBaseUrl() {
        return entityBaseUrl;
    }

    public Boolean getWantAssertionsSigned() {
        return wantAssertionsSigned;
    }

    public Optional<String> getRoleAttributeMapping() {
        return Optional.ofNullable(roleAttributeMapping);
    }

    public Optional<String> getEncryptionCertFilePath() {
        return Optional.ofNullable(encryptionCertFilePath);
    }

    public Optional<String> getEncryptionPrivateKeyFilePath() {
        return Optional.ofNullable(encryptionPrivateKeyFilePath);
    }

    public Optional<String> getSigningCertFilePath() {
        return Optional.ofNullable(signingCertFilePath);
    }

    public Optional<String> getSigningPrivateKeyFilePath() {
        return Optional.ofNullable(signingPrivateKeyFilePath);
    }

    public Optional<String> getVerificationCertFilePath() {
        return Optional.ofNullable(verificationCertFilePath);
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
