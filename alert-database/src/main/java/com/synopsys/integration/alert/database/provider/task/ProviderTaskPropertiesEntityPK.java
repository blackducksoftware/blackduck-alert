/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.provider.task;

import java.io.Serializable;

public class ProviderTaskPropertiesEntityPK implements Serializable {
    private String taskName;
    private String propertyName;

    public ProviderTaskPropertiesEntityPK() {
        // JPA requires default constructor definitions
    }

    public ProviderTaskPropertiesEntityPK(String taskName, String propertyName) {
        this.taskName = taskName;
        this.propertyName = propertyName;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getPropertyName() {
        return propertyName;
    }

}
