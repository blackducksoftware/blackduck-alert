/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.DatabaseModelWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface ConfigurationAccessor<T extends AlertSerializableModel> {
    Optional<DatabaseModelWrapper<T>> getConfiguration(UUID id);

    AlertPagedModel<DatabaseModelWrapper<T>> getConfigurationPage(int page, int size);

    DatabaseModelWrapper<T> createConfiguration(T configuration);

    DatabaseModelWrapper<T> updateConfiguration(UUID configurationId, T configuration) throws AlertConfigurationException;

    void deleteConfiguration(UUID configurationId);

}
