/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.saml.model;

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;

import com.blackduck.integration.alert.api.common.model.Obfuscated;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.model.ConfigWithMetadata;

public class SAMLConfigModel extends ConfigWithMetadata implements Obfuscated<SAMLConfigModel> {
    private Boolean enabled;
    private Boolean forceAuth;
    private String metadataUrl;
    private String metadataFileName;
    private SAMLMetadataMode metadataMode;
    private String encryptionCertFileName;
    private String encryptionPrivateKeyFileName;
    private String signingCertFileName;
    private String signingPrivateKeyFileName;
    private String verificationCertFileName;

    public SAMLConfigModel() {
        // For serialization
    }

    // Required
    public SAMLConfigModel(String id) {
        super(id, AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    public SAMLConfigModel(
        String id,
        String createdAt,
        String lastUpdated,
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
        this(id);
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
            metadataFileName,
            metadataMode,
            encryptionCertFileName,
            encryptionPrivateKeyFileName,
            signingCertFileName,
            signingPrivateKeyFileName,
            verificationCertFileName
        );
    }

    // Getters
    public Boolean getEnabled() {
        return BooleanUtils.toBoolean(enabled);
    }

    public Boolean getForceAuth() {
        return BooleanUtils.toBoolean(forceAuth);
    }

    public Optional<String> getMetadataUrl() {
        return Optional.ofNullable(metadataUrl);
    }

    public Optional<String> getMetadataFileName() {
        return Optional.ofNullable(metadataFileName);
    }

    public Optional<SAMLMetadataMode> getMetadataMode() {
        return Optional.ofNullable(metadataMode);
    }

    public Optional<String> getEncryptionCertFileName() {
        return Optional.ofNullable(encryptionCertFileName);
    }

    public Optional<String> getEncryptionPrivateKeyFileName() {
        return Optional.ofNullable(encryptionPrivateKeyFileName);
    }

    public Optional<String> getSigningCertFileName() {
        return Optional.ofNullable(signingCertFileName);
    }

    public Optional<String> getSigningPrivateKeyFileName() {
        return Optional.ofNullable(signingPrivateKeyFileName);
    }

    public Optional<String> getVerificationCertFileName() {
        return Optional.ofNullable(verificationCertFileName);
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
}
