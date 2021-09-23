/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel2;

public interface ConfigurationAccessor2<T extends AlertSerializableModel> {
    Optional<ConfigurationModel2<T>> getConfiguration(Long id);

    List<ConfigurationModel2<T>> getAllConfigurations();

    ConfigurationModel2<T> createConfiguration(T configuration);

    ConfigurationModel2<T> updateConfiguration(Long configurationId, T configuration) throws AlertConfigurationException;

    void deleteConfiguration(Long configurationId);

}
