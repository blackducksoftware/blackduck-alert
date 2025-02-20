/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.settings.proxy.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.blackduck.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;

@Component
public class SettingsProxyValidationAction {
    private final SettingsProxyValidator validator;
    private final ConfigurationValidationHelper validationHelper;

    @Autowired
    public SettingsProxyValidationAction(SettingsProxyValidator validator, AuthorizationManager authorizationManager, SettingsDescriptorKey settingsDescriptorKey) {
        this.validator = validator;
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, settingsDescriptorKey);
    }

    public ActionResponse<ValidationResponseModel> validate(SettingsProxyModel requestResource) {
        return validationHelper.validate(() -> validator.validate(requestResource));
    }
}
