package com.blackduck.integration.alert.channel.azure.boards.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.AzureBoardsChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.blackduck.integration.alert.common.descriptor.config.ConcreteGlobalConfigExistsValidator;

@Component
public class AzureBoardsGlobalConfigExistsValidator implements ConcreteGlobalConfigExistsValidator {
    private final AzureBoardsChannelKey azureBoardsChannelKey;
    private final AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;

    @Autowired
    public AzureBoardsGlobalConfigExistsValidator(
        AzureBoardsChannelKey azureBoardsChannelKey,
        AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor
    ) {
        this.azureBoardsChannelKey = azureBoardsChannelKey;
        this.azureBoardsGlobalConfigAccessor = azureBoardsGlobalConfigAccessor;
    }

    @Override
    public boolean exists() { return azureBoardsGlobalConfigAccessor.getConfigurationCount() > 0; }

    @Override
    public DescriptorKey getDescriptorKey() { return azureBoardsChannelKey; }
}
