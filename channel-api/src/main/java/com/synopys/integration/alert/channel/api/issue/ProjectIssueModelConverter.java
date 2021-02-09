/*
 * channel-api
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
package com.synopys.integration.alert.channel.api.issue;

import java.io.Serializable;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopys.integration.alert.channel.api.convert.LinkableItemConverter;
import com.synopys.integration.alert.channel.api.issue.model.IssueCommentModel;
import com.synopys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopys.integration.alert.channel.api.issue.model.IssueTransitionModel;
import com.synopys.integration.alert.channel.api.issue.model.ProjectIssueModel;

public class ProjectIssueModelConverter {
    private final IssueTrackerMessageFormatter formatter;
    private final LinkableItemConverter linkableItemConverter;

    public ProjectIssueModelConverter(IssueTrackerMessageFormatter formatter) {
        this.formatter = formatter;
        this.linkableItemConverter = new LinkableItemConverter(formatter);
    }

    public IssueCreationModel toIssueCreationModel(ProjectIssueModel projectIssueModel) {
        // FIXME implement
        return null;
    }

    public <T extends Serializable> IssueTransitionModel<T> toIssueTransitionModel(T issueId, ProjectIssueModel projectIssueModel, ItemOperation requiredOperation) {
        // FIXME implement
        return null;
    }

    public <T extends Serializable> IssueCommentModel<T> toIssueCommentModel(T issueId, ProjectIssueModel projectIssueModel) {
        // FIXME implement
        return null;
    }

}
