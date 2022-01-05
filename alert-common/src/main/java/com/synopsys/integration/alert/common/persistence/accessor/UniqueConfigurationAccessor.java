/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;

public interface UniqueConfigurationAccessor<T extends AlertSerializableModel> {
    Optional<T> getConfiguration();

    T createConfiguration(T configuration) throws AlertConfigurationException;

    T updateConfiguration(T configuration) throws AlertConfigurationException;

    void deleteConfiguration();
}
