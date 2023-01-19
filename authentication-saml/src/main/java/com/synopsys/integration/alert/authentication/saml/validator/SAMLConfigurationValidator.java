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

        // Perform validation on fields if enabled
        if (model.getEnabled().orElse(false)) {
            // Convert to empty optional for blank metadata url and file path
            Optional<String> optionalFilteredMetadataUrl = model.getMetadataUrl().filter(StringUtils::isNotBlank);
            Optional<String> optionalFilteredMetadataFilePath = model.getMetadataFilePath().filter(StringUtils::isNotBlank);
            boolean metadataFileExists = optionalFilteredMetadataFilePath
                .map(filePersistenceUtil::uploadFileExists)
                .orElse(false);

            if (StringUtils.isBlank(model.getEntityId())) {
                statuses.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_ENTITY_ID, AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
            }
            if (StringUtils.isBlank(model.getEntityBaseUrl())) {
                statuses.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL, AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
            } else {
                addErrorStatusIfInvalidUrl(model.getEntityBaseUrl(), AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL, statuses);
            }

            // One of url or filepath must exist
            if (optionalFilteredMetadataUrl.isEmpty() && !metadataFileExists) {
                statuses.add(AlertFieldStatus.error(
                    AuthenticationDescriptor.KEY_SAML_METADATA_FILE,
                    AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_FILE_MISSING)
                );
            }
            // Check if valid url for present, else add missing status if metadata file is also missing
            optionalFilteredMetadataUrl.ifPresentOrElse(
                metaDataUrl -> addErrorStatusIfInvalidUrl(metaDataUrl, AuthenticationDescriptor.KEY_SAML_METADATA_URL, statuses),
                () -> {
                    if (!metadataFileExists) {
                        statuses.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_METADATA_URL, AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
                    }
                }
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
