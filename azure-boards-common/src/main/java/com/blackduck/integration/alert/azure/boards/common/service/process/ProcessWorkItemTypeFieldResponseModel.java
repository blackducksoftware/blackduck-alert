/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.process;

public class ProcessWorkItemTypeFieldResponseModel {
    private boolean allowGroups;
    private String description;
    private String name;
    private boolean readOnly;
    private String referenceName;
    private boolean required;
    private String url;

    public ProcessWorkItemTypeFieldResponseModel() {
        // for serialization
    }

    public ProcessWorkItemTypeFieldResponseModel(
        boolean allowGroups,
        String description,
        String name,
        boolean readOnly,
        String referenceName,
        boolean required,
        String url
    ) {
        this.allowGroups = allowGroups;
        this.description = description;
        this.name = name;
        this.readOnly = readOnly;
        this.referenceName = referenceName;
        this.required = required;
        this.url = url;
    }

    public boolean isAllowGroups() {
        return allowGroups;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public boolean isRequired() {
        return required;
    }

    public String getUrl() {
        return url;
    }
}
