/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.descriptor.accessor;

import java.util.Optional;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;

public interface SettingsUtility {
    //TODO: This is used in AlertStartupInitializer::initializeConfigs and is set to be Deprecated
    DescriptorKey getKey();

    Optional<SettingsProxyModel> getConfiguration();
}
