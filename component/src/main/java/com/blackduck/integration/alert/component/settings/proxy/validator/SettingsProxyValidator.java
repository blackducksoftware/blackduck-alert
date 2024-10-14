/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.settings.proxy.validator;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;

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

        if (StringUtils.isBlank(model.getProxyHost())) {
            statuses.add(AlertFieldStatus.error(PROXY_HOST_FIELD_NAME, AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (model.getProxyPort() == null) {
            statuses.add(AlertFieldStatus.error(PROXY_PORT_FIELD_NAME, AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (model.getProxyUsername().filter(StringUtils::isNotBlank).isPresent() && !BooleanUtils.toBoolean(model.getIsProxyPasswordSet())) {
            validateRequiredFieldIsNotBlank(statuses, model.getProxyPassword().isPresent(), PROXY_PASSWORD_FIELD_NAME);

        }

        if (model.getProxyPassword().isPresent() || BooleanUtils.toBoolean(model.getIsProxyPasswordSet())) {
            validateRequiredFieldIsNotBlank(statuses, model.getProxyUsername().isPresent(), PROXY_USERNAME_FIELD_NAME);
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
