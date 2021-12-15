/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.proxy.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.UniqueConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.component.settings.proxy.model.SettingsProxyModel;
import com.synopsys.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;

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
            () -> configurationAccessor.getConfigurationByName(UniqueConfigurationAccessor.DEFAULT_CONFIGURATION_NAME));
    }

    public ActionResponse<SettingsProxyModel> create(SettingsProxyModel resource) {
        return configurationHelper.create(
            () -> validator.validate(resource),
            () -> configurationAccessor.createConfiguration(resource)
        );
    }

    public ActionResponse<SettingsProxyModel> update(SettingsProxyModel requestResource) {
        return configurationHelper.update(
            () -> validator.validate(requestResource),
            () -> configurationAccessor.getConfigurationByName(UniqueConfigurationAccessor.DEFAULT_CONFIGURATION_NAME).isPresent(),
            () -> configurationAccessor.updateConfiguration(requestResource)
        );
    }

    public ActionResponse<SettingsProxyModel> delete() {
        return configurationHelper.delete(
            () -> configurationAccessor.getConfigurationByName(UniqueConfigurationAccessor.DEFAULT_CONFIGURATION_NAME).isPresent(),
            () -> configurationAccessor.deleteConfiguration(UniqueConfigurationAccessor.DEFAULT_CONFIGURATION_NAME)
        );
    }
}
