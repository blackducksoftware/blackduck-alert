/*
 * channel
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
package com.synopsys.integration.alert.channel.jira2.cloud;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopys.integration.alert.channel.api.issue.IssueTrackerSearcher;
import com.synopys.integration.alert.channel.api.issue.model.IssueSearchResult;
import com.synopys.integration.alert.channel.api.issue.model.ProjectIssueModel;

@Component
public class JiraCloudSearcher extends IssueTrackerSearcher<String> {
    @Override
    protected List<IssueSearchResult<String>> findIssuesByProject(LinkableItem provider, LinkableItem project) throws AlertException {
        // FIXME implement
        return List.of();
    }

    @Override
    protected List<IssueSearchResult<String>> findIssuesByProjectAndVersion(LinkableItem provider, LinkableItem project, LinkableItem projectVersion) throws AlertException {
        // FIXME implement
        return List.of();
    }

    @Override
    protected List<IssueSearchResult<String>> findIssuesByComponent(LinkableItem provider, LinkableItem project, LinkableItem projectVersion, LinkableItem component, @Nullable LinkableItem componentVersion) throws AlertException {
        // FIXME implement
        return List.of();
    }

    @Override
    protected IssueSearchResult<String> findIssueByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException {
        // FIXME implement
        return new IssueSearchResult<>(null, projectIssueModel, ItemOperation.ADD);
    }

}
