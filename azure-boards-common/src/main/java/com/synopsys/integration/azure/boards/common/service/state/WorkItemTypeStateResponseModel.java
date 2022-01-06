/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.state;

public class WorkItemTypeStateResponseModel {
    private String name;
    private String color;
    private String category;

    public WorkItemTypeStateResponseModel() {
        // For serialization
    }

    public WorkItemTypeStateResponseModel(String name, String color, String category) {
        this.name = name;
        this.color = color;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String getCategory() {
        return category;
    }

}
