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
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.MessageReason;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;
import com.synopys.integration.alert.channel.api.issue.model.ActionableIssueSearchResult;
import com.synopys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopys.integration.alert.channel.api.issue.model.ProjectIssueSearchResult;

public abstract class IssueTrackerSearcher<T extends Serializable> {
    public final List<ActionableIssueSearchResult<T>> findIssues(ProjectMessage projectMessage) throws AlertException {
        LinkableItem provider = projectMessage.getProvider();
        LinkableItem project = projectMessage.getProject();

        MessageReason messageReason = projectMessage.getMessageReason();
        boolean isEntireBomDeleted = projectMessage.getOperation()
                                         .filter(ProjectOperation.DELETE::equals)
                                         .isPresent();

        if (MessageReason.PROJECT_STATUS.equals(messageReason)) {
            if (isEntireBomDeleted) {
                return findProjectIssues(provider, project)
                           .stream()
                           .map(this::convertToDeleteResult)
                           .collect(Collectors.toList());
            }
            return List.of();
        }

        LinkableItem projectVersion = projectMessage.getProjectVersion()
                                          .orElseThrow(() -> new AlertRuntimeException("Missing project version"));
        if (MessageReason.PROJECT_VERSION_STATUS.equals(messageReason)) {
            if (isEntireBomDeleted) {
                return findProjectVersionIssues(provider, project, projectVersion)
                           .stream()
                           .map(this::convertToDeleteResult)
                           .collect(Collectors.toList());
            }
            return List.of();
        }

        if (MessageReason.COMPONENT_UPDATE.equals(messageReason)) {
            return findIssuesByAllComponents(provider, project, projectVersion, projectMessage.getBomComponents());
        }

        List<ProjectIssueModel> projectIssueModels = ProjectMessageToIssueModelTransformer.convertToIssueModels(projectMessage);

        List<ActionableIssueSearchResult<T>> projectIssueSearchResults = new LinkedList<>();
        for (ProjectIssueModel projectIssueModel : projectIssueModels) {
            ActionableIssueSearchResult<T> searchResult = findIssueByProjectIssueModel(projectIssueModel);
            projectIssueSearchResults.add(searchResult);
        }
        return projectIssueSearchResults;
    }

    protected abstract List<ProjectIssueSearchResult<T>> findProjectIssues(LinkableItem provider, LinkableItem project) throws AlertException;

    protected abstract List<ProjectIssueSearchResult<T>> findProjectVersionIssues(LinkableItem provider, LinkableItem project, LinkableItem projectVersion) throws AlertException;

    protected abstract List<ProjectIssueSearchResult<T>> findIssuesByComponent(LinkableItem provider, LinkableItem project, LinkableItem projectVersion, BomComponentDetails bomComponent) throws AlertException;

    protected abstract ActionableIssueSearchResult<T> findIssueByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException;

    private List<ActionableIssueSearchResult<T>> findIssuesByAllComponents(LinkableItem provider, LinkableItem project, LinkableItem projectVersion, List<BomComponentDetails> bomComponents) throws AlertException {
        List<ProjectIssueSearchResult<T>> componentIssues = new LinkedList<>();
        for (BomComponentDetails bomComponent : bomComponents) {
            List<ProjectIssueSearchResult<T>> issuesByComponent = findIssuesByComponent(provider, project, projectVersion, bomComponent);
            componentIssues.addAll(issuesByComponent);
        }
        return componentIssues
                   .stream()
                   .map(this::convertToUpdateResult)
                   .collect(Collectors.toList());
    }

    private ActionableIssueSearchResult<T> convertToDeleteResult(ProjectIssueSearchResult<T> projectIssueSearchResult) {
        return convertToOperationResult(projectIssueSearchResult, ItemOperation.DELETE);
    }

    private ActionableIssueSearchResult<T> convertToUpdateResult(ProjectIssueSearchResult<T> projectIssueSearchResult) {
        return convertToOperationResult(projectIssueSearchResult, ItemOperation.UPDATE);
    }

    private ActionableIssueSearchResult<T> convertToOperationResult(ProjectIssueSearchResult<T> projectIssueSearchResult, ItemOperation operation) {
        return new ActionableIssueSearchResult<>(projectIssueSearchResult.getIssueId(), projectIssueSearchResult.getProjectIssueModel(), operation);
    }

}
