package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Optional;

public interface ProviderTaskPropertiesAccessor {
    Optional<String> getTaskProperty(String taskName, String propertyKey);

    void setTaskProperty(Long configId, String taskName, String propertyKey, String propertyValue);

}
