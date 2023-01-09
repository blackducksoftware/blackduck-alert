package com.synopsys.integration.alert.authentication.saml.validator;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class SAMLConfigurationValidator {
    public ValidationResponseModel validate(SAMLConfigModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();

        // Perform validation on fields if enabled
        if (model.getEnabled().orElse(false)) {
            if (StringUtils.isBlank(model.getEntityId())) {
                statuses.add(AlertFieldStatus.error("entityId", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
            }
            if (StringUtils.isBlank(model.getEntityBaseUrl())) {
                statuses.add(AlertFieldStatus.error("entityBaseUrl", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
            } else {
                addErrorStatusIfInvalidUrl(model.getEntityBaseUrl(), "entityBaseUrl", statuses);
            }

            // Convert to empty optional for blank metadata url and file path
            Optional<String> optionalFilteredMetadataUrl = model.getMetadataUrl().filter(StringUtils::isNotBlank);
            Optional<String> optionalFilteredMetadataFilePath = model.getMetadataFilePath().filter(StringUtils::isNotBlank);
            // One of url or filepath must exist
            if (optionalFilteredMetadataUrl.isEmpty() && optionalFilteredMetadataFilePath.isEmpty()) {  // TODO: verify message
                statuses.add(AlertFieldStatus.error("metadataUrl, metadataFilePath", "One of either fields is required."));
            }
            // Check if the metadata fields are valid
            optionalFilteredMetadataUrl.ifPresent(
                metaDataUrl -> addErrorStatusIfInvalidUrl(metaDataUrl, "metaDataUrl", statuses)
            );
            optionalFilteredMetadataFilePath.ifPresent(
                metaDataFilePath -> addErrorStatusIfInvalidMetadataFilePath(metaDataFilePath, statuses)
            );
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }

    private void addErrorStatusIfInvalidUrl(String url, String fieldName, Set<AlertFieldStatus> statuses) {
        if (StringUtils.isNotBlank(url)) {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                statuses.add(AlertFieldStatus.error(fieldName, e.getMessage()));
            }
        }
    }

    // Could make this more generic with param field name but lint warns since it is only one value here
    private void addErrorStatusIfInvalidMetadataFilePath(String filePath, Set<AlertFieldStatus> statuses) {
        File file = new File(filePath);
        if (!file.isFile()) {   // TODO: Verify if we're using AuthenticationConfigurationFieldModelValidator and FilePersistenceUtil.uploadFileExists
            statuses.add(AlertFieldStatus.error("metaDataFilePath", "Metadata file path is incorrect or does not point to a file"));
        }
    }
}
