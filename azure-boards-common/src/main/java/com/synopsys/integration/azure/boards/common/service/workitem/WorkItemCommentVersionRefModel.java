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
