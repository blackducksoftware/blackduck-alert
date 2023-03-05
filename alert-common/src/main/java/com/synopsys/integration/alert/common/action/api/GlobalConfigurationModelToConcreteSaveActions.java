/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action.api;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.api.descriptor.model.DescriptorKey;

/**
 * @deprecated This class is required for converting an old ConfigurationModel into the new GlobalConfigModel classes. This is a temporary class that should be removed once we
 * remove unsupported REST endpoints in 8.0.0.
 */
@Deprecated(forRemoval = true)
public interface GlobalConfigurationModelToConcreteSaveActions {

    DescriptorKey getDescriptorKey();

    void updateConcreteModel(ConfigurationModel configurationModel);

    void createConcreteModel(ConfigurationModel configurationModel);

    void deleteConcreteModel(ConfigurationModel configurationModel);
}
