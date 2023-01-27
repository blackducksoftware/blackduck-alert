package com.synopsys.integration.alert.authentication.saml.validator;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
        // Just check if file is upload - filePath is for showing to user their uploaded path and may not need to validate it
        boolean metadataFileExists = filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_METADATA_FILE);

        if (StringUtils.isBlank(model.getEntityId())) {
            statuses.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_ENTITY_ID, AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (StringUtils.isBlank(model.getEntityBaseUrl())) {
            statuses.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL, AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        } else {
            addErrorStatusIfInvalidUrl(model.getEntityBaseUrl(), AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL, statuses);
        }

        // One of url or file must exist
        if (optionalMetadataUrl.isEmpty() && !metadataFileExists) {
            statuses.add(AlertFieldStatus.error(
                AuthenticationDescriptor.KEY_SAML_METADATA_FILE, AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_FILE_MISSING)
            );
        }
        // Check if valid url for present, else add missing status if metadata file is also missing
        optionalMetadataUrl.ifPresentOrElse(
            metaDataUrl -> addErrorStatusIfInvalidUrl(metaDataUrl, AuthenticationDescriptor.KEY_SAML_METADATA_URL, statuses),
            () -> {
                if (!metadataFileExists) {
                    statuses.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_METADATA_URL, AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
                }
            }
        );

        // For advanced settings, check if the following have their public keys uploaded if the cert is
        boolean signingCertFileExists = filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE);
        boolean encryptionCertFileExists = filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE);

        if (signingCertFileExists && !filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_SIGNING_PRIVATE_KEY_FILE)) {
            statuses.add(AlertFieldStatus.error(
                "signingCertFilePath", "SAML signing private key has not been uploaded for signing certificate.")
            );
        }
        if (encryptionCertFileExists && !filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE)) {
            statuses.add(AlertFieldStatus.error(
                "verificationCertFilePath", "SAML encryption private key has not been uploaded for encryption certificate.")
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
