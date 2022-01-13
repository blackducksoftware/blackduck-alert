/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.proxy.validator;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;

@Component
public class SettingsProxyValidator {
    public static final String PROXY_CONFIGURATION_NAME = "name";
    public static final String PROXY_HOST_FIELD_NAME = "proxyHost";
    public static final String PROXY_PORT_FIELD_NAME = "proxyPort";
    public static final String PROXY_USERNAME_FIELD_NAME = "proxyUsername";
    public static final String PROXY_PASSWORD_FIELD_NAME = "proxyPassword";

    public ValidationResponseModel validate(SettingsProxyModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        validateRequiredFieldIsNotBlank(statuses, StringUtils.isNotBlank(model.getName()), PROXY_CONFIGURATION_NAME);

        if (model.getProxyPort().isEmpty() && model.getProxyHost().isEmpty()) {
            validateRequiredFieldIsNotBlank(statuses, model.getProxyHost().isPresent(), PROXY_HOST_FIELD_NAME);
            validateRequiredFieldIsNotBlank(statuses, model.getProxyPort().isPresent(), PROXY_PORT_FIELD_NAME);
        }

        if (model.getProxyHost().isPresent() && model.getProxyPort().isEmpty()) {
            validateRequiredFieldIsNotBlank(statuses, model.getProxyPort().isPresent(), PROXY_PORT_FIELD_NAME);
        }

        if (model.getProxyPort().isPresent() && model.getProxyHost().isEmpty()) {
            validateRequiredFieldIsNotBlank(statuses, model.getProxyHost().isPresent(), PROXY_HOST_FIELD_NAME);
        }

        if (model.getProxyUsername().isPresent()) {
            validateRequiredFieldIsNotBlank(statuses, model.getProxyHost().isPresent(), PROXY_HOST_FIELD_NAME);
            if (!BooleanUtils.toBoolean(model.getIsProxyPasswordSet())) {
                validateRequiredFieldIsNotBlank(statuses, model.getProxyPassword().isPresent(), PROXY_PASSWORD_FIELD_NAME);
            }
        }

        if (model.getProxyPassword().isPresent() || BooleanUtils.toBoolean(model.getIsProxyPasswordSet())) {
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
