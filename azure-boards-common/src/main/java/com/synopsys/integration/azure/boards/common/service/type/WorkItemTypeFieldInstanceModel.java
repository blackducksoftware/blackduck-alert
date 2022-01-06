/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.type;

import java.util.List;

import com.synopsys.integration.azure.boards.common.service.workitem.WorkItemFieldReferenceModel;

public class WorkItemTypeFieldInstanceModel {
    private String name;
    private String referenceName;
    private String helpText;
    private String defaultValue;
    private List<String> allowedValues;
    private Boolean alwaysRequired;
    private List<WorkItemFieldReferenceModel> dependentFields;
    private String url;

    public WorkItemTypeFieldInstanceModel() {
        // For serialization
    }

    public WorkItemTypeFieldInstanceModel(String name, String referenceName, String helpText, String defaultValue, List<String> allowedValues, Boolean alwaysRequired,
        List<WorkItemFieldReferenceModel> dependentFields, String url) {
        this.name = name;
        this.referenceName = referenceName;
        this.helpText = helpText;
        this.defaultValue = defaultValue;
        this.allowedValues = allowedValues;
        this.alwaysRequired = alwaysRequired;
        this.dependentFields = dependentFields;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getHelpText() {
        return helpText;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public Boolean getAlwaysRequired() {
        return alwaysRequired;
    }

    public List<WorkItemFieldReferenceModel> getDependentFields() {
        return dependentFields;
    }

    public String getUrl() {
        return url;
    }

}
