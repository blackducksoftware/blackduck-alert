/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface ConfigurationAccessor<T extends AlertSerializableModel> {
    long getConfigurationCount();

    Optional<T> getConfiguration(UUID id);

    Optional<T> getConfigurationByName(String configurationName);

    AlertPagedModel<T> getConfigurationPage(int page, int size);

    T createConfiguration(T configuration);

    T updateConfiguration(UUID configurationId, T configuration) throws AlertConfigurationException;

    void deleteConfiguration(UUID configurationId);

}
