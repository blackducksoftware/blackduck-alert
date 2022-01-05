/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action.api;

import java.util.Optional;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;

public interface GlobalConfigurationModelToConcreteConverter<T extends ConfigWithMetadata> {
    Optional<T> convert(ConfigurationModel globalConfigurationModel);
}
