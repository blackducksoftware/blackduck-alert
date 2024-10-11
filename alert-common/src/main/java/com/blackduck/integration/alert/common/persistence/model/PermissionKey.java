/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class PermissionKey extends AlertSerializableModel {
    private final String context;
    private final String descriptorName;

    public PermissionKey(String context, String descriptorName) {
        this.context = context;
        this.descriptorName = descriptorName;
    }

    public String getContext() {
        return context;
    }

    public String getDescriptorName() {
        return descriptorName;
    }

}
