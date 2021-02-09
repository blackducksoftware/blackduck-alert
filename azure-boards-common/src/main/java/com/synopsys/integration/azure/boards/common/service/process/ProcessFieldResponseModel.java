/*
 * azure-boards-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.azure.boards.common.service.process;

import com.google.gson.JsonObject;

public class ProcessFieldResponseModel {
    private Boolean allowGroups;
    private String customization;
    private JsonObject defaultValue;
    private String description;
    private String name;
    private Boolean readOnly;
    private String referenceName;
    private Boolean required;
    private String type;
    private String url;

    public ProcessFieldResponseModel() {
        // For serialization
    }

    public ProcessFieldResponseModel(Boolean allowGroups, String customization, JsonObject defaultValue, String description, String name, Boolean readOnly, String referenceName, Boolean required, String type, String url) {
        this.allowGroups = allowGroups;
        this.customization = customization;
        this.defaultValue = defaultValue;
        this.description = description;
        this.name = name;
        this.readOnly = readOnly;
        this.referenceName = referenceName;
        this.required = required;
        this.type = type;
        this.url = url;
    }

    public Boolean getAllowGroups() {
        return allowGroups;
    }

    public String getCustomization() {
        return customization;
    }

    public JsonObject getDefaultValue() {
        return defaultValue;
    }

    public boolean isDefaultValueAString() {
        return defaultValue.isJsonPrimitive() && defaultValue.getAsJsonPrimitive().isString();
    }

    public String getDefaultValueAsString() {
        return defaultValue.getAsJsonPrimitive().getAsString();
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
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

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

}
