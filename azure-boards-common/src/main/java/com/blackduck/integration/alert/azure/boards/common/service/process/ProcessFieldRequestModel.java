/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.process;

public class ProcessFieldRequestModel {
    private Boolean allowGroups;
    private Object defaultValue;
    private Boolean readOnly;
    private String referenceName;
    private Boolean required;

    private ProcessFieldRequestModel() {
        // For serialization
    }

    public ProcessFieldRequestModel(Boolean allowGroups, Object defaultValue, Boolean readOnly, String referenceName, Boolean required) {
        this.allowGroups = allowGroups;
        this.defaultValue = defaultValue;
        this.readOnly = readOnly;
        this.referenceName = referenceName;
        this.required = required;
    }

    public Boolean getAllowGroups() {
        return allowGroups;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public Boolean getRequired() {
        return required;
    }

}
