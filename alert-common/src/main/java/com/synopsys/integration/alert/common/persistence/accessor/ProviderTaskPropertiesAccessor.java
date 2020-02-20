package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Optional;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;

public interface ProviderTaskPropertiesAccessor {
    Optional<String> getTaskProperty(String taskName, String propertyKey);

    void setTaskProperty(Long configId, String taskName, String propertyKey, String propertyValue) throws AlertDatabaseConstraintException;

}
