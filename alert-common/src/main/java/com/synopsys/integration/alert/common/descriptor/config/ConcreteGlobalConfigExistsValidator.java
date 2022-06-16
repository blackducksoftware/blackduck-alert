package com.synopsys.integration.alert.common.descriptor.config;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public interface ConcreteGlobalConfigExistsValidator {
    boolean exists();

    DescriptorKey getDescriptorKey();
}
