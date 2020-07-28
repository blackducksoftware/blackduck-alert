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
package com.synopsys.integration.azure.boards.common.service.project;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;

public class ProjectWorkItemFieldResponseModel {
    private Map<String, ReferenceLinkModel> _links;
    private Boolean canSortBy;
    private String description;
    private Boolean isIdentity;
    private Boolean isPicklist;
    private Boolean isPicklistSuggested;
    private Boolean isQueryable;
    private String name;
    private String picklistId;
    private Boolean readOnly;
    private String referenceName;
    private List<JsonObject> supportedOperations;
    private String type;
    private String url;

    public ProjectWorkItemFieldResponseModel() {
        // For serialization
    }

    public ProjectWorkItemFieldResponseModel(Map<String, ReferenceLinkModel> _links, Boolean canSortBy, String description, Boolean isIdentity, Boolean isPicklist, Boolean isPicklistSuggested, Boolean isQueryable, String name,
        String picklistId, Boolean readOnly, String referenceName, List<JsonObject> supportedOperations, String type, String url) {
        this._links = _links;
        this.canSortBy = canSortBy;
        this.description = description;
        this.isIdentity = isIdentity;
        this.isPicklist = isPicklist;
        this.isPicklistSuggested = isPicklistSuggested;
        this.isQueryable = isQueryable;
        this.name = name;
        this.picklistId = picklistId;
        this.readOnly = readOnly;
        this.referenceName = referenceName;
        this.supportedOperations = supportedOperations;
        this.type = type;
        this.url = url;
    }

    public Map<String, ReferenceLinkModel> getLinks() {
        return _links;
    }

    public Boolean getCanSortBy() {
        return canSortBy;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getIdentity() {
        return isIdentity;
    }

    public Boolean getPicklist() {
        return isPicklist;
    }

    public Boolean getPicklistSuggested() {
        return isPicklistSuggested;
    }

    public Boolean getQueryable() {
        return isQueryable;
    }

    public String getName() {
        return name;
    }

    public String getPicklistId() {
        return picklistId;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public List<JsonObject> getSupportedOperations() {
        return supportedOperations;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

}
