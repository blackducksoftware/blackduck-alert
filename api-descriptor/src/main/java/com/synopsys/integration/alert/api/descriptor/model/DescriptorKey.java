/*
 * api-descriptor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.descriptor.model;

import java.io.Serializable;

import com.blackduck.integration.util.Stringable;

public abstract class DescriptorKey extends Stringable implements Serializable {

    private final String universalKey;
    private final String displayName;

    protected DescriptorKey(String universalKey, String displayName) {
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
