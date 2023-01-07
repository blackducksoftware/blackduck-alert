package com.synopsys.integration.alert.authentication.saml.validator;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SAMLConfigurationValidator {
    public ValidationResponseModel validate(SAMLConfigModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();

        if (model.getEntityId().isEmpty()) {
            statuses.add(AlertFieldStatus.error("entityId", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (model.getEntityBaseUrl().isEmpty()) {
            statuses.add(AlertFieldStatus.error("entityBaseUrl", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        // One of url or filepath must exist
        if (model.getMetadataUrl().isEmpty() && model.getMetadataFilePath().isEmpty()) {
            statuses.add(AlertFieldStatus.error("metadataUrl", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
            statuses.add(AlertFieldStatus.error("metadataFilePath", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }
}
