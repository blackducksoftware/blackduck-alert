package com.synopsys.integration.alert.authentication.saml.validator;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.authentication.saml.model.AuthenticationSAMLConfigModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AuthenticationSAMLConfigurationValidator {
    public ValidationResponseModel validate(AuthenticationSAMLConfigModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();

        if (StringUtils.isBlank(model.getName())) {
            statuses.add(AlertFieldStatus.error("name", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }
}
