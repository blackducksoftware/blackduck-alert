/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.project;

public class ProjectPropertyResponseModel {
    public static final String COMMON_PROPERTIES_PROCESS_ID = "System.ProcessTemplateType";

    private String name;
    private String value;

    public ProjectPropertyResponseModel() {
        // For serialization
    }

    public ProjectPropertyResponseModel(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
