/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.convert;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteSaveActions;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.proxy.action.SettingsProxyCrudActions;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class ProxyConfigurationModelSaveActions implements GlobalConfigurationModelToConcreteSaveActions {
    private final ProxyConfigurationModelConverter proxyFieldModelConverter;
    private final SettingsProxyCrudActions configurationActions;

    public ProxyConfigurationModelSaveActions(ProxyConfigurationModelConverter proxyFieldModelConverter, SettingsProxyCrudActions configurationActions) {
        this.proxyFieldModelConverter = proxyFieldModelConverter;
        this.configurationActions = configurationActions;
    }

    @Override
    public DescriptorKey getDescriptorKey() {
        return new SettingsDescriptorKey();
    }

    @Override
    public void updateConcreteModel(ConfigurationModel configurationModel) {
        convertModelAndPerformAction(configurationModel, configurationActions::update);
    }

    @Override
    public void createConcreteModel(ConfigurationModel configurationModel) {
        convertModelAndPerformAction(configurationModel, configurationActions::create);
    }

    @Override
    public void deleteConcreteModel(ConfigurationModel configurationModel) {
        configurationActions.delete();
    }

    private void convertModelAndPerformAction(ConfigurationModel configurationModel, Function<SettingsProxyModel, ActionResponse<SettingsProxyModel>> configAction) {
        Optional<SettingsProxyModel> settingsProxyModel = proxyFieldModelConverter.convert(configurationModel);
        if (settingsProxyModel.isPresent()) {
            SettingsProxyModel model = settingsProxyModel.get();
            model.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
            configAction.apply(model);
        }
    }
}
