/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.descriptor.model;

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
