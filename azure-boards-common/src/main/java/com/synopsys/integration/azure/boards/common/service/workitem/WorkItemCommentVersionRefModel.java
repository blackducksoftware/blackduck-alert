/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.workitem;

public class WorkItemCommentVersionRefModel {
    private Integer commentId;
    private String text;
    private Integer version;
    private Integer createdInRevision;
    private Boolean isDeleted;
    private String url;

    public WorkItemCommentVersionRefModel() {
        // For serialization
    }

    public WorkItemCommentVersionRefModel(Integer commentId, String text, Integer version, Integer createdInRevision, Boolean isDeleted, String url) {
        this.commentId = commentId;
        this.text = text;
        this.version = version;
        this.createdInRevision = createdInRevision;
        this.isDeleted = isDeleted;
        this.url = url;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public String getText() {
        return text;
    }

    public Integer getVersion() {
        return version;
    }

    public Integer getCreatedInRevision() {
        return createdInRevision;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public String getUrl() {
        return url;
    }

}
