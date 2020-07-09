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
package com.synopsys.integration.azure.boards.common.service.workitem.response;

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
