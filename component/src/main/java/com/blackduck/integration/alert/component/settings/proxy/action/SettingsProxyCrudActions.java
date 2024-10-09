/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.settings.proxy.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.blackduck.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;

@Component
public class SettingsProxyCrudActions {
    private final ConfigurationCrudHelper configurationHelper;
    private final SettingsProxyConfigAccessor configurationAccessor;
    private final SettingsProxyValidator validator;

    @Autowired
    public SettingsProxyCrudActions(AuthorizationManager authorizationManager, SettingsProxyConfigAccessor configurationAccessor, SettingsProxyValidator validator, SettingsDescriptorKey settingsDescriptorKey) {
        this.configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, settingsDescriptorKey);
        this.configurationAccessor = configurationAccessor;
        this.validator = validator;
    }

    public ActionResponse<SettingsProxyModel> getOne() {
        return configurationHelper.getOne(
            configurationAccessor::getConfiguration);
    }

    public ActionResponse<SettingsProxyModel> create(SettingsProxyModel resource) {
        return configurationHelper.create(
            () -> validator.validate(resource),
            configurationAccessor::doesConfigurationExist,
            () -> configurationAccessor.createConfiguration(resource)
        );
    }

    public ActionResponse<SettingsProxyModel> update(SettingsProxyModel requestResource) {
        return configurationHelper.update(
            () -> validator.validate(requestResource),
            configurationAccessor::doesConfigurationExist,
            () -> configurationAccessor.updateConfiguration(requestResource)
        );
    }

    public ActionResponse<SettingsProxyModel> delete() {
        return configurationHelper.delete(
            configurationAccessor::doesConfigurationExist,
            configurationAccessor::deleteConfiguration
        );
    }
}
