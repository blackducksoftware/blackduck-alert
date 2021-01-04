/**
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
package com.synopsys.integration.azure.boards.common.service.workitem;

import java.util.Map;

import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;

public class WorkItemUserModel {
    private String id;
    private String displayName;
    private String uniqueName;
    private String descriptor;
    private String imageUrl;
    private String url;
    private Boolean isDeletedInOrigin;
    private Map<String, ReferenceLinkModel> _links;

    public WorkItemUserModel() {
        // For serialization
    }

    public WorkItemUserModel(String id, String displayName, String uniqueName, String descriptor, String imageUrl, String url, Boolean isDeletedInOrigin, Map<String, ReferenceLinkModel> _links) {
        this.id = id;
        this.displayName = displayName;
        this.uniqueName = uniqueName;
        this.descriptor = descriptor;
        this.imageUrl = imageUrl;
        this.url = url;
        this.isDeletedInOrigin = isDeletedInOrigin;
        this._links = _links;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public Boolean getDeletedInOrigin() {
        return isDeletedInOrigin;
    }

    public Map<String, ReferenceLinkModel> getLinks() {
        return _links;
    }

}
