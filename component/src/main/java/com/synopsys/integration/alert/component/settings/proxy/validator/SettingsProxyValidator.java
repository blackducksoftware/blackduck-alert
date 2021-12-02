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
    public static final String PROXY_HOST_FIELD_NAME = "proxyHost";
    public static final String PROXY_PORT_FIELD_NAME = "proxyPort";
    public static final String PROXY_USERNAME_FIELD_NAME = "proxyUsername";
    public static final String PROXY_PASSWORD_FIELD_NAME = "proxyPassword";

    public ValidationResponseModel validate(SettingsProxyModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        if (model.getProxyHost().isPresent() && model.getProxyPort().isEmpty()) {
            validateRequiredFieldIsNotBlank(statuses, model.getProxyPort().isPresent(), PROXY_PORT_FIELD_NAME);
        }

        if (model.getProxyPort().isPresent() && model.getProxyHost().isEmpty()) {
            validateRequiredFieldIsNotBlank(statuses, model.getProxyHost().isPresent(), PROXY_HOST_FIELD_NAME);
        }

        if (model.getProxyUsername().isPresent()) {
            validateRequiredFieldIsNotBlank(statuses, model.getProxyHost().isPresent(), PROXY_HOST_FIELD_NAME);
            validateRequiredFieldIsNotBlank(statuses, model.getProxyPassword().isPresent(), PROXY_PASSWORD_FIELD_NAME);
        }

        if (model.getProxyPassword().isPresent()) {
            validateRequiredFieldIsNotBlank(statuses, model.getProxyHost().isPresent(), PROXY_HOST_FIELD_NAME);
            validateRequiredFieldIsNotBlank(statuses, model.getProxyUsername().isPresent(), PROXY_USERNAME_FIELD_NAME);
        }

        if (model.getNonProxyHosts().isPresent()) {
            validateRequiredFieldIsNotBlank(statuses, model.getProxyHost().isPresent(), PROXY_HOST_FIELD_NAME);
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
