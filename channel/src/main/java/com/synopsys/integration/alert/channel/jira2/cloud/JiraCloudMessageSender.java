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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.model.request.IssueCommentRequestModel;
import com.synopys.integration.alert.channel.api.issue.IssueTrackerMessageSender;
import com.synopys.integration.alert.channel.api.issue.model.ExistingIssueDetails;
import com.synopys.integration.alert.channel.api.issue.model.IssueCommentModel;
import com.synopys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopys.integration.alert.channel.api.issue.model.IssueTransitionModel;
import com.synopys.integration.alert.channel.api.issue.model.ProjectIssueModel;

public class JiraCloudMessageSender extends IssueTrackerMessageSender<JiraCloudJobDetailsModel, String> {
    private final IssueService issueService;

    public JiraCloudMessageSender(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    protected List<IssueTrackerIssueResponseModel> createIssues(JiraCloudJobDetailsModel details, List<IssueCreationModel> issueCreationModels) throws AlertException {
        // FIXME implement
        return List.of();
    }

    @Override
    protected List<IssueTrackerIssueResponseModel> transitionIssues(JiraCloudJobDetailsModel details, List<IssueTransitionModel<String>> issueTransitionModels) throws AlertException {
        // FIXME implement
        return List.of();
    }

    @Override
    protected List<IssueTrackerIssueResponseModel> commentOnIssues(JiraCloudJobDetailsModel details, List<IssueCommentModel<String>> issueCommentModels) throws AlertException {
        List<IssueTrackerIssueResponseModel> responses = new LinkedList<>();
        for (IssueCommentModel<String> issueCommentModel : issueCommentModels) {
            ExistingIssueDetails<String> existingIssueDetails = issueCommentModel.getExistingIssueDetails();
            for (String comment : issueCommentModel.getComments()) {
                addComment(existingIssueDetails.getIssueKey(), comment);
            }

            AlertIssueOrigin alertIssueOrigin = createIssueOrigin(issueCommentModel.getSource());
            IssueTrackerIssueResponseModel commentResponse = new IssueTrackerIssueResponseModel(alertIssueOrigin, existingIssueDetails.getIssueKey(), existingIssueDetails.getIssueLink(), existingIssueDetails.getIssueSummary(),
                IssueOperation.UPDATE);
            responses.add(commentResponse);
        }
        return responses;
    }

    private void addComment(String issueKey, String commentContent) throws AlertException {
        IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, commentContent);
        try {
            issueService.addComment(issueCommentRequestModel);
        } catch (IntegrationException e) {
            throw new AlertException("Failed to add a comment in Jira", e);
        }
    }

    // FIXME replace this concept
    private AlertIssueOrigin createIssueOrigin(ProjectIssueModel projectIssueModel) {
        LinkableItem provider = projectIssueModel.getProvider();
        LinkableItem project = projectIssueModel.getProject();
        LinkableItem projectVersion = projectIssueModel.getProjectVersion()
                                          .orElseThrow(() -> new AlertRuntimeException("Missing BlackDuck project-version"));
        ContentKey providerContentKey = new ContentKey(
            provider.getLabel(),
            null,
            project.getLabel(),
            project.getValue(),
            projectVersion.getLabel(),
            projectVersion.getValue(),
            ItemOperation.UPDATE
        );

        BomComponentDetails bomComponent = projectIssueModel.getBomComponent();
        ComponentConcern arbitraryComponentConcern = bomComponent.getComponentConcerns()
                                                         .stream()
                                                         .findAny()
                                                         .orElseThrow(() -> new AlertRuntimeException("Missing component-concern"));
        String categoryString = StringUtils.capitalize(arbitraryComponentConcern.getType().name().toLowerCase());

        ComponentItem componentItem;
        try {
            componentItem = new ComponentItem.Builder()
                                .applyCategory(categoryString)
                                .applyOperation(arbitraryComponentConcern.getOperation())
                                .applyComponentData(bomComponent.getComponent())
                                .applySubComponent(bomComponent.getComponentVersion().orElse(null))
                                .applyCategoryItem(categoryString, arbitraryComponentConcern.getName())
                                .applyNotificationId(0L)
                                .build();
        } catch (AlertException e) {
            throw new AlertRuntimeException(e);
        }
        return new AlertIssueOrigin(providerContentKey, componentItem);
    }

}
