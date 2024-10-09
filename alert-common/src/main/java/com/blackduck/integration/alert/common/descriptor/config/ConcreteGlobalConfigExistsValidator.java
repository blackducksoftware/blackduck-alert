package com.blackduck.integration.alert.common.descriptor.config;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;

public interface ConcreteGlobalConfigExistsValidator {
    boolean exists();

    DescriptorKey getDescriptorKey();
}
