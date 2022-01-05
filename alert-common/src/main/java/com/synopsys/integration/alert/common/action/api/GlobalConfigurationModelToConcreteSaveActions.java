/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action.api;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public interface GlobalConfigurationModelToConcreteSaveActions {

    DescriptorKey getDescriptorKey();

    void updateConcreteModel(ConfigurationModel configurationModel);

    void createConcreteModel(ConfigurationModel configurationModel);

    void deleteConcreteModel(ConfigurationModel configurationModel);
}
