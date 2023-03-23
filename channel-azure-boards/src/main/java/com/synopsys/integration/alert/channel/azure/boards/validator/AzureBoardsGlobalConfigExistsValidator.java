package com.synopsys.integration.alert.channel.azure.boards.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.common.descriptor.config.ConcreteGlobalConfigExistsValidator;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

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
