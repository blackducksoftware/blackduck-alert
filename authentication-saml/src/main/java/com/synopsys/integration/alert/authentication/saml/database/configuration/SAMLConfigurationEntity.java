package com.synopsys.integration.alert.authentication.saml.database.configuration;

import com.synopsys.integration.alert.authentication.saml.model.SAMLMetadataMode;
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
    private boolean enabled;
    @Column(name = "force_auth")
    private boolean forceAuth;
    @Column(name = "metadata_url")
    private String metadataUrl;
    @Column(name = "metadata_file_path")
    private String metadataFilePath;
    @Column(name = "metadata_mode")
    private SAMLMetadataMode metadataMode;
    @Column(name = "want_assertions_signed")
    private boolean wantAssertionsSigned;
    @Column(name = "encryption_cert_file_path")
    private String encryptionCertFilePath;
    @Column(name = "encryption_private_key_file_path")
    private String encryptionPrivateKeyFilePath;
    @Column(name = "signing_cert_file_path")
    private String signingCertFilePath;
    @Column(name = "signing_private_key_file_path")
    private String signingPrivateKeyFilePath;
    @Column(name = "verification_cert_file_path")
    private String verificationCertFilePath;

    public SAMLConfigurationEntity() {
    }

    public SAMLConfigurationEntity(
        UUID configurationId,
        OffsetDateTime createdAt,
        OffsetDateTime lastUpdated,
        boolean enabled,
        boolean forceAuth,
        String metadataUrl,
        String metadataFilePath,
        SAMLMetadataMode metadataMode,
        boolean wantAssertionsSigned,
        String encryptionCertFilePath,
        String encryptionPrivateKeyFilePath,
        String signingCertFilePath,
        String signingPrivateKeyFilePath,
        String verificationCertFilePath
    ) {
        this.configurationId = configurationId;
        this.name = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.enabled = enabled;
        this.forceAuth = forceAuth;
        this.metadataUrl = metadataUrl;
        this.metadataFilePath = metadataFilePath;
        this.metadataMode = metadataMode;
        this.wantAssertionsSigned = wantAssertionsSigned;
        this.encryptionCertFilePath = encryptionCertFilePath;
        this.encryptionPrivateKeyFilePath = encryptionPrivateKeyFilePath;
        this.signingCertFilePath = signingCertFilePath;
        this.signingPrivateKeyFilePath = signingPrivateKeyFilePath;
        this.verificationCertFilePath = verificationCertFilePath;
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

    public boolean getEnabled() {
        return enabled;
    }

    public boolean getForceAuth() {
        return forceAuth;
    }

    public String getMetadataUrl() {
        return metadataUrl;
    }

    public String getMetadataFilePath() {
        return metadataFilePath;
    }

    public SAMLMetadataMode getMetadataMode() {
        return metadataMode;
    }

    public boolean getWantAssertionsSigned() {
        return wantAssertionsSigned;
    }

    public String getEncryptionCertFilePath() {
        return encryptionCertFilePath;
    }

    public String getEncryptionPrivateKeyFilePath() {
        return encryptionPrivateKeyFilePath;
    }

    public String getSigningCertFilePath() {
        return signingCertFilePath;
    }

    public String getSigningPrivateKeyFilePath() {
        return signingPrivateKeyFilePath;
    }

    public String getVerificationCertFilePath() {
        return verificationCertFilePath;
    }
}
