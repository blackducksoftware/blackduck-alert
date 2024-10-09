package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Optional;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;

public interface UniqueConfigurationAccessor<T extends AlertSerializableModel> {
    Optional<T> getConfiguration();

    boolean doesConfigurationExist();

    T createConfiguration(T configuration) throws AlertConfigurationException;

    T updateConfiguration(T configuration) throws AlertConfigurationException;

    void deleteConfiguration();
}
