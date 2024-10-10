/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Optional;
import java.util.UUID;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

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
