/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.settings.encryption.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.blackduck.integration.alert.component.settings.encryption.model.SettingsEncryptionModel;
import com.blackduck.integration.alert.component.settings.encryption.validator.SettingsEncryptionValidator;

@Component
public class SettingsEncryptionValidationAction {
    private final SettingsEncryptionValidator validator;
    private final ConfigurationValidationHelper validationHelper;

    @Autowired
    public SettingsEncryptionValidationAction(SettingsEncryptionValidator validator, AuthorizationManager authorizationManager, SettingsDescriptorKey settingsDescriptorKey) {
        this.validator = validator;
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, settingsDescriptorKey);
    }

    public ActionResponse<ValidationResponseModel> validate(SettingsEncryptionModel requestResource) {
        return validationHelper.validate(() -> validator.validate(requestResource));
    }

}
