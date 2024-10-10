/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.saml.validator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.blackduck.integration.alert.authentication.saml.model.SAMLMetadataMode;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;

@Component
public class SAMLConfigurationValidator {
    private final FilePersistenceUtil filePersistenceUtil;

    @Autowired
    public SAMLConfigurationValidator(FilePersistenceUtil filePersistenceUtil) {
        this.filePersistenceUtil = filePersistenceUtil;
    }

    public ValidationResponseModel validate(SAMLConfigModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();

        Optional<String> optionalMetadataUrl = model.getMetadataUrl().filter(StringUtils::isNotBlank);
        // Just check if file is upload - fileName is for showing to user their uploaded name and may not need to validate it
        boolean metadataFileExists = filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_METADATA_FILE);
        SAMLMetadataMode samlMetadataMode = model.getMetadataMode().orElse(SAMLMetadataMode.URL);
        // One of url or file must exist
        if (samlMetadataMode == SAMLMetadataMode.FILE && !metadataFileExists) {
            statuses.add(AlertFieldStatus.error(
                "metadataFileName", AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_FILE_MISSING)
            );
        }
        // Check if valid url for present for metadata URL mode
        if (samlMetadataMode == SAMLMetadataMode.URL) {
            optionalMetadataUrl.ifPresentOrElse(
                metaDataUrl -> addErrorStatusIfInvalidUrl(metaDataUrl, "metadataUrl", statuses),
                () -> statuses.add(AlertFieldStatus.error("metadataUrl", AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_URL_MISSING))
            );
        }

        // For advanced settings, check if the following have their private keys uploaded if the cert is
        boolean signingCertFileExists = filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE);
        boolean encryptionCertFileExists = filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE);

        if (signingCertFileExists && !filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_SIGNING_PRIVATE_KEY_FILE)) {
            statuses.add(AlertFieldStatus.error(
                "signingPrivateKeyFileName", "SAML signing private key has not been uploaded for signing certificate.")
            );
        }
        if (encryptionCertFileExists && !filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE)) {
            statuses.add(AlertFieldStatus.error(
                "encryptionPrivateKeyFileName", "SAML encryption private key has not been uploaded for encryption certificate.")
            );
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }

    private void addErrorStatusIfInvalidUrl(String url, String fieldName, Set<AlertFieldStatus> statuses) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            statuses.add(AlertFieldStatus.error(fieldName, e.getMessage()));
        }
    }
}
