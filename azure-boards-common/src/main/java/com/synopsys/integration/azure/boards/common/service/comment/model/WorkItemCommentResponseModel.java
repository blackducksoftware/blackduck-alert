/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.comment.model;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;
import com.synopsys.integration.azure.boards.common.service.workitem.WorkItemUserModel;

public class WorkItemCommentResponseModel {
    private Integer id;
    private String text;
    private Integer version;
    private String url;
    private Integer workItemId;
    private WorkItemUserModel createdBy;
    private String createdDate;
    private String createdOnBehalfDate;
    private WorkItemUserModel createdOnBehalfOf;
    private Boolean isDeleted;
    private List<JsonObject> mentions;
    private WorkItemUserModel modifiedBy;
    private String modifiedDate;
    private List<JsonObject> reactions;
    private Map<String, ReferenceLinkModel> _links;

    public WorkItemCommentResponseModel() {
        // For serialization
    }

    public WorkItemCommentResponseModel(Integer id, String text, Integer version, String url, Integer workItemId, WorkItemUserModel createdBy, String createdDate, String createdOnBehalfDate,
        WorkItemUserModel createdOnBehalfOf, Boolean isDeleted, List<JsonObject> mentions, WorkItemUserModel modifiedBy, String modifiedDate, List<JsonObject> reactions,
        Map<String, ReferenceLinkModel> _links) {
        this.id = id;
        this.text = text;
        this.version = version;
        this.url = url;
        this.workItemId = workItemId;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.createdOnBehalfDate = createdOnBehalfDate;
        this.createdOnBehalfOf = createdOnBehalfOf;
        this.isDeleted = isDeleted;
        this.mentions = mentions;
        this.modifiedBy = modifiedBy;
        this.modifiedDate = modifiedDate;
        this.reactions = reactions;
        this._links = _links;
    }

    public Integer getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Integer getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public Integer getWorkItemId() {
        return workItemId;
    }

    public WorkItemUserModel getCreatedBy() {
        return createdBy;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getCreatedOnBehalfDate() {
        return createdOnBehalfDate;
    }

    public WorkItemUserModel getCreatedOnBehalfOf() {
        return createdOnBehalfOf;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public List<JsonObject> getMentions() {
        return mentions;
    }

    public WorkItemUserModel getModifiedBy() {
        return modifiedBy;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public List<JsonObject> getReactions() {
        return reactions;
    }

    public Map<String, ReferenceLinkModel> getLinks() {
        return _links;
    }

}
