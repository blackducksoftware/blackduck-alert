/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
