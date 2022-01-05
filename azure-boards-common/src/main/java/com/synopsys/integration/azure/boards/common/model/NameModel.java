/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.model;

public class NameModel {
    private String name;

    public NameModel() {
        // For serialization
    }

    public NameModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
