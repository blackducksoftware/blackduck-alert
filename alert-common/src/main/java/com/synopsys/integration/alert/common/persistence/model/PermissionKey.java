/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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
