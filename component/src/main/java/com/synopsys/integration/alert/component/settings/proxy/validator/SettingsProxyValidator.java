package com.synopsys.integration.alert.component.settings.proxy.validator;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.component.settings.proxy.model.SettingsProxyModel;

@Component
public class SettingsProxyValidator {
    private static final String PROXY_HOST_FIELD_NAME = "proxyHost";
    private static final String PROXY_PORT_FIELD_NAME = "proxyPort";
    private static final String PROXY_USERNAME_FIELD_NAME = "proxyUsername";
    private static final String PROXY_PASSWORD_FIELD_NAME = "proxyPassword";

    public ValidationResponseModel validate(SettingsProxyModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        if (model.getHost().isPresent() && model.getPort().isEmpty()) {
            validateRequiredFieldIsNotBlank(statuses, model.getPort().isPresent(), PROXY_PORT_FIELD_NAME);
        }

        if (model.getPort().isPresent() && model.getHost().isEmpty()) {
            validateRequiredFieldIsNotBlank(statuses, model.getHost().isPresent(), PROXY_HOST_FIELD_NAME);
        }

        if (model.getUsername().isPresent()) {
            validateRequiredFieldIsNotBlank(statuses, model.getHost().isPresent(), PROXY_HOST_FIELD_NAME);
            validateRequiredFieldIsNotBlank(statuses, model.getPassword().isPresent(), PROXY_PASSWORD_FIELD_NAME);
        }

        if (model.getPassword().isPresent()) {
            validateRequiredFieldIsNotBlank(statuses, model.getHost().isPresent(), PROXY_HOST_FIELD_NAME);
            validateRequiredFieldIsNotBlank(statuses, model.getUsername().isPresent(), PROXY_USERNAME_FIELD_NAME);
        }

        if (model.getNonProxyHosts().isPresent()) {
            validateRequiredFieldIsNotBlank(statuses, model.getHost().isPresent(), PROXY_HOST_FIELD_NAME);
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }

    private void validateRequiredFieldIsNotBlank(Set<AlertFieldStatus> statuses, boolean isFieldPresent, String fieldName) {
        if (!isFieldPresent) {
            statuses.add(AlertFieldStatus.error(fieldName, AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
    }
}
