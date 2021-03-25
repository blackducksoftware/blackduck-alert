/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.action;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public interface DescriptorAction {
    DescriptorKey getDescriptorKey();

}
