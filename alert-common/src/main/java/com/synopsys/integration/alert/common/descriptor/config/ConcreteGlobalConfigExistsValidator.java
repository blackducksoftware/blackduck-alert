/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public interface ConcreteGlobalConfigExistsValidator {
    boolean exists();

    DescriptorKey getDescriptorKey();
}
