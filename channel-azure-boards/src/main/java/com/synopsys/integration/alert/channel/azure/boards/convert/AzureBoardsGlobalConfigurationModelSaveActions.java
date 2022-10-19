package com.synopsys.integration.alert.channel.azure.boards.convert;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.action.AzureBoardsGlobalCrudActions;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteSaveActions;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

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
        Optional<AzureBoardsGlobalConfigModel> azureBoardsGlobalConfigModel = azureBoardsModelConverter.convertAndValidate(configurationModel);
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
        Optional<AzureBoardsGlobalConfigModel> azureBoardsGlobalConfigModel = azureBoardsModelConverter.convertAndValidate(configurationModel);
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
