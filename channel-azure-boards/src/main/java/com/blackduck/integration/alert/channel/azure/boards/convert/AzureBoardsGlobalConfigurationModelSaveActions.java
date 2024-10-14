/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.convert;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.channel.azure.boards.action.AzureBoardsGlobalCrudActions;
import com.blackduck.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.alert.common.action.api.GlobalConfigurationModelToConcreteSaveActions;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;

/**
 * @deprecated This class is required for converting an old ConfigurationModel into the new GlobalConfigModel classes. This is a temporary class that should be removed once we
 * remove unsupported REST endpoints in 8.0.0.
 */
@Component
@Deprecated(forRemoval = true)
public class AzureBoardsGlobalConfigurationModelSaveActions implements GlobalConfigurationModelToConcreteSaveActions {
    private final AzureBoardsGlobalConfigurationModelConverter azureBoardsModelConverter;
    private final AzureBoardsGlobalCrudActions configurationActions;
    private final AzureBoardsGlobalConfigAccessor configurationAccessor;

    @Autowired
    public AzureBoardsGlobalConfigurationModelSaveActions(
        AzureBoardsGlobalConfigurationModelConverter azureBoardsModelConverter,
        AzureBoardsGlobalCrudActions configurationActions,
        AzureBoardsGlobalConfigAccessor configurationAccessor
    ) {
        this.azureBoardsModelConverter = azureBoardsModelConverter;
        this.configurationActions = configurationActions;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public DescriptorKey getDescriptorKey() {
        return ChannelKeys.AZURE_BOARDS;
    }

    @Override
    public void updateConcreteModel(ConfigurationModel configurationModel) {
        Optional<UUID> defaultConfigurationId = configurationAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .map(AzureBoardsGlobalConfigModel::getId)
            .map(UUID::fromString);
        Optional<AzureBoardsGlobalConfigModel> azureBoardsGlobalConfigModel = azureBoardsModelConverter.convertAndValidate(
            configurationModel,
            defaultConfigurationId.map(UUID::toString).orElse(null)
        );
        if (azureBoardsGlobalConfigModel.isPresent()) {
            AzureBoardsGlobalConfigModel model = azureBoardsGlobalConfigModel.get();
            model.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
            if (defaultConfigurationId.isPresent()) {
                configurationActions.update(defaultConfigurationId.get(), model);
            } else {
                configurationActions.create(model);
            }
        }
    }

    @Override
    public void createConcreteModel(ConfigurationModel configurationModel) {
        Optional<AzureBoardsGlobalConfigModel> azureBoardsGlobalConfigModel = azureBoardsModelConverter.convertAndValidate(configurationModel, null);
        if (azureBoardsGlobalConfigModel.isPresent()) {
            AzureBoardsGlobalConfigModel model = azureBoardsGlobalConfigModel.get();
            model.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
            configurationActions.create(model);
        }
    }

    @Override
    public void deleteConcreteModel(ConfigurationModel configurationModel) {
        Optional<UUID> defaultConfigurationId = configurationAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .map(AzureBoardsGlobalConfigModel::getId)
            .map(UUID::fromString);
        defaultConfigurationId.ifPresent(configurationActions::delete);
    }
}
