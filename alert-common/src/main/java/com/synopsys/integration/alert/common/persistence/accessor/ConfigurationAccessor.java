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

    boolean existsConfigurationByName(String configurationName);

    boolean existsConfigurationById(UUID id);

    AlertPagedModel<T> getConfigurationPage(
        int page, int size, String searchTerm, String sortName, String sortOrder
    );

    T createConfiguration(T configuration) throws AlertConfigurationException;

    T updateConfiguration(UUID configurationId, T configuration) throws AlertConfigurationException;

    void deleteConfiguration(UUID configurationId);

}
