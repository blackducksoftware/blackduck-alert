package com.blackduck.integration.alert.authentication.saml.database.configuration;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.blackduck.integration.alert.authentication.saml.model.SAMLMetadataMode;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.database.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
    @Column(name = "metadata_file_name")
    private String metadataFileName;
    @Column(name = "metadata_mode")
    private SAMLMetadataMode metadataMode;
    @Column(name = "encryption_cert_file_name")
    private String encryptionCertFileName;
    @Column(name = "encryption_private_key_file_name")
    private String encryptionPrivateKeyFileName;
    @Column(name = "signing_cert_file_name")
    private String signingCertFileName;
    @Column(name = "signing_private_key_file_name")
    private String signingPrivateKeyFileName;
    @Column(name = "verification_cert_file_name")
    private String verificationCertFileName;

    public SAMLConfigurationEntity() {
    }

    public SAMLConfigurationEntity(
        UUID configurationId,
        OffsetDateTime createdAt,
        OffsetDateTime lastUpdated,
        Boolean enabled,
        Boolean forceAuth,
        String metadataUrl,
        String metadataFileName,
        SAMLMetadataMode metadataMode,
        String encryptionCertFileName,
        String encryptionPrivateKeyFileName,
        String signingCertFileName,
        String signingPrivateKeyFileName,
        String verificationCertFileName
    ) {
        this.configurationId = configurationId;
        this.name = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.enabled = enabled;
        this.forceAuth = forceAuth;
        this.metadataUrl = metadataUrl;
        this.metadataFileName = metadataFileName;
        this.metadataMode = metadataMode;
        this.encryptionCertFileName = encryptionCertFileName;
        this.encryptionPrivateKeyFileName = encryptionPrivateKeyFileName;
        this.signingCertFileName = signingCertFileName;
        this.signingPrivateKeyFileName = signingPrivateKeyFileName;
        this.verificationCertFileName = verificationCertFileName;
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

    public String getMetadataFileName() {
        return metadataFileName;
    }

    public SAMLMetadataMode getMetadataMode() {
        return metadataMode;
    }

    public String getEncryptionCertFileName() {
        return encryptionCertFileName;
    }

    public String getEncryptionPrivateKeyFileName() {
        return encryptionPrivateKeyFileName;
    }

    public String getSigningCertFileName() {
        return signingCertFileName;
    }

    public String getSigningPrivateKeyFileName() {
        return signingPrivateKeyFileName;
    }

    public String getVerificationCertFileName() {
        return verificationCertFileName;
    }
}
