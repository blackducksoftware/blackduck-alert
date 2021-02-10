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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.MessageReason;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopys.integration.alert.channel.api.issue.model.IssueSearchResult;
import com.synopys.integration.alert.channel.api.issue.model.ProjectIssueModel;

public abstract class IssueTrackerSearcher<T extends Serializable> {
    public final List<IssueSearchResult<T>> findIssues(ProjectMessage projectMessage) throws AlertException {
        LinkableItem provider = projectMessage.getProvider();
        LinkableItem project = projectMessage.getProject();

        MessageReason messageReason = projectMessage.getMessageReason();

        if (MessageReason.PROJECT_STATUS.equals(messageReason)) {
            return findIssuesByProject(provider, project);
        }

        LinkableItem projectVersion = projectMessage.getProjectVersion()
                                          .orElseThrow(() -> new AlertRuntimeException("Missing project version"));
        if (MessageReason.PROJECT_VERSION_STATUS.equals(messageReason)) {
            return findIssuesByProjectAndVersion(provider, project, projectVersion);
        }

        if (MessageReason.COMPONENT_UPDATE.equals(messageReason)) {
            return findIssuesByAllComponents(provider, project, projectVersion, projectMessage.getBomComponents());
        }

        List<ProjectIssueModel> projectIssueModels = ProjectMessageToIssueModelTransformer.convertToIssueModels(projectMessage);

        List<IssueSearchResult<T>> projectIssueSearchResults = new LinkedList<>();
        for (ProjectIssueModel projectIssueModel : projectIssueModels) {
            findIssueByProjectIssueModel(projectIssueModel).ifPresent(projectIssueSearchResults::add);
        }
        return projectIssueSearchResults;
    }

    protected abstract List<IssueSearchResult<T>> findIssuesByProject(LinkableItem provider, LinkableItem project) throws AlertException;

    protected abstract List<IssueSearchResult<T>> findIssuesByProjectAndVersion(LinkableItem provider, LinkableItem project, LinkableItem projectVersion) throws AlertException;

    protected abstract List<IssueSearchResult<T>> findIssuesByComponent(LinkableItem provider, LinkableItem project, LinkableItem projectVersion, LinkableItem component, @Nullable LinkableItem componentVersion) throws AlertException;

    protected abstract Optional<IssueSearchResult<T>> findIssueByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException;

    private List<IssueSearchResult<T>> findIssuesByAllComponents(LinkableItem provider, LinkableItem project, LinkableItem projectVersion, List<BomComponentDetails> bomComponents) throws AlertException {
        List<IssueSearchResult<T>> componentIssues = new LinkedList<>();
        for (BomComponentDetails bomComponent : bomComponents) {
            LinkableItem component = bomComponent.getComponent();
            LinkableItem nullableComponentVersion = bomComponent.getComponentVersion().orElse(null);
            List<IssueSearchResult<T>> issuesByComponent = findIssuesByComponent(provider, project, projectVersion, component, nullableComponentVersion);
            componentIssues.addAll(issuesByComponent);
        }
        return componentIssues;
    }

}
