/*
 * api-descriptor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.descriptor.api.model;

import java.io.Serializable;

import com.synopsys.integration.util.Stringable;

public abstract class DescriptorKey extends Stringable implements Serializable {

    private final String universalKey;
    private final String displayName;

    public DescriptorKey(String universalKey, String displayName) {
        this.universalKey = universalKey;
        this.displayName = displayName;
    }

    public String getUniversalKey() {
        return universalKey;
    }

    public String getDisplayName() {
        return displayName;
    }

}
