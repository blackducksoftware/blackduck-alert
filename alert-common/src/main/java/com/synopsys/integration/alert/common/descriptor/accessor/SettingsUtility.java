/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.accessor;

import java.util.Optional;

import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public interface SettingsUtility {
    //TODO: This is used in AlertStartupInitializer::initializeConfigs and is set to be Deprecated
    DescriptorKey getKey();

    Optional<SettingsProxyModel> getConfiguration();
}
