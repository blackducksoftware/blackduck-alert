/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.query;

public class SupportedQueryOperationModel {
    private String name;
    private String referenceName;

    public SupportedQueryOperationModel() {
        // For serialization
    }

    public SupportedQueryOperationModel(String name, String referenceName) {
        this.name = name;
        this.referenceName = referenceName;
    }

    public String getName() {
        return name;
    }

    public String getReferenceName() {
        return referenceName;
    }

}
