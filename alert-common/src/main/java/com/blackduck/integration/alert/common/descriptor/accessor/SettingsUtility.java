package com.blackduck.integration.alert.common.descriptor.accessor;

import java.util.Optional;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;

public interface SettingsUtility {
    //TODO: This is used in AlertStartupInitializer::initializeConfigs and is set to be Deprecated
    DescriptorKey getKey();

    Optional<SettingsProxyModel> getConfiguration();
}
