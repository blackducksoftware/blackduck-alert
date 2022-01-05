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

import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;

public class WorkItemMultiCommentResponseModel {
    private Integer count;
    private Integer totalCount;
    private String continuationToken;
    private String nextPage;
    private List<WorkItemCommentResponseModel> comments;
    private String url;
    private Map<String, ReferenceLinkModel> _links;

    public WorkItemMultiCommentResponseModel() {
        // For serialization
    }

    public WorkItemMultiCommentResponseModel(Integer count, Integer totalCount, String continuationToken, String nextPage,
        List<WorkItemCommentResponseModel> comments, String url, Map<String, ReferenceLinkModel> _links) {
        this.count = count;
        this.totalCount = totalCount;
        this.continuationToken = continuationToken;
        this.nextPage = nextPage;
        this.comments = comments;
        this.url = url;
        this._links = _links;
    }

    public Integer getCount() {
        return count;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public String getNextPage() {
        return nextPage;
    }

    public List<WorkItemCommentResponseModel> getComments() {
        return comments;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, ReferenceLinkModel> getLinks() {
        return _links;
    }

}
