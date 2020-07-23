/**
 * azure-boards-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
