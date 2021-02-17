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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.channel.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.channel.jira.common.util.JiraCallbackUtils;
import com.synopsys.integration.alert.channel.jira2.common.JiraErrorMessageUtility;
import com.synopsys.integration.alert.channel.jira2.common.JiraIssueCreationRequestCreator;
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
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.model.request.IssueCommentRequestModel;
import com.synopsys.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.synopsys.integration.jira.common.model.response.IssueCreationResponseModel;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopys.integration.alert.channel.api.issue.IssueTrackerMessageSender;
import com.synopys.integration.alert.channel.api.issue.model.ExistingIssueDetails;
import com.synopys.integration.alert.channel.api.issue.model.IssueCommentModel;
import com.synopys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopys.integration.alert.channel.api.issue.model.IssueTransitionModel;
import com.synopys.integration.alert.channel.api.issue.model.ProjectIssueModel;

public class JiraCloudMessageSender extends IssueTrackerMessageSender<String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JiraCloudJobDetailsModel distributionDetails;
    private final IssueService issueService;
    private final JiraIssueCreationRequestCreator jiraIssueCreationRequestCreator;
    private final JiraIssueAlertPropertiesManager issuePropertiesManager;
    private final JiraErrorMessageUtility jiraErrorMessageUtility;

    public JiraCloudMessageSender(
        JiraCloudJobDetailsModel distributionDetails,
        IssueService issueService,
        JiraIssueCreationRequestCreator jiraIssueCreationRequestCreator,
        JiraIssueAlertPropertiesManager issuePropertiesManager,
        JiraErrorMessageUtility jiraErrorMessageUtility
    ) {
        this.distributionDetails = distributionDetails;
        this.issueService = issueService;
        this.jiraIssueCreationRequestCreator = jiraIssueCreationRequestCreator;
        this.issuePropertiesManager = issuePropertiesManager;
        this.jiraErrorMessageUtility = jiraErrorMessageUtility;
    }

    @Override
    protected List<IssueTrackerIssueResponseModel> createIssues(List<IssueCreationModel> issueCreationModels) throws AlertException {
        List<IssueTrackerIssueResponseModel> responses = new LinkedList<>();
        for (IssueCreationModel alertIssueCreationModel : issueCreationModels) {
            // FIXME get all field values
            IssueRequestModelFieldsMapBuilder fieldsBuilder = jiraIssueCreationRequestCreator.createIssueRequestModel(
                alertIssueCreationModel.getTitle(),
                alertIssueCreationModel.getDescription(),
                null,
                distributionDetails.getIssueType(),
                null,
                distributionDetails.getCustomFields()
            );
            IssueCreationRequestModel creationRequestModel = new IssueCreationRequestModel(
                distributionDetails.getIssueCreatorEmail(),
                distributionDetails.getIssueType(),
                distributionDetails.getProjectNameOrKey(),
                fieldsBuilder,
                List.of()
            );
            IssueTrackerIssueResponseModel responseModel = createIssue(alertIssueCreationModel, creationRequestModel);
            responses.add(responseModel);
        }
        return responses;
    }

    @Override
    protected List<IssueTrackerIssueResponseModel> transitionIssues(List<IssueTransitionModel<String>> issueTransitionModels) throws AlertException {
        // FIXME implement
        return List.of();
    }

    @Override
    protected List<IssueTrackerIssueResponseModel> commentOnIssues(List<IssueCommentModel<String>> issueCommentModels) throws AlertException {
        List<IssueTrackerIssueResponseModel> responses = new LinkedList<>();
        for (IssueCommentModel<String> issueCommentModel : issueCommentModels) {
            ExistingIssueDetails<String> existingIssueDetails = issueCommentModel.getExistingIssueDetails();
            addComments(existingIssueDetails.getIssueKey(), issueCommentModel.getComments());

            AlertIssueOrigin alertIssueOrigin = createIssueOrigin(issueCommentModel.getSource());
            IssueTrackerIssueResponseModel commentResponse = new IssueTrackerIssueResponseModel(
                alertIssueOrigin,
                existingIssueDetails.getIssueKey(),
                existingIssueDetails.getIssueLink(),
                existingIssueDetails.getIssueSummary(),
                IssueOperation.UPDATE
            );
            responses.add(commentResponse);
        }
        return responses;
    }

    private IssueTrackerIssueResponseModel createIssue(IssueCreationModel alertIssueModel, IssueCreationRequestModel creationRequest) throws AlertException {
        IssueResponseModel createdIssue;
        AlertIssueOrigin issueOrigin = null;
        try {
            IssueCreationResponseModel issueCreationResponseModel = issueService.createIssue(creationRequest);
            createdIssue = issueService.getIssue(issueCreationResponseModel.getKey());

            String issueKey = createdIssue.getKey();
            logger.debug("Created new Jira Cloud issue: {}", issueKey);

            Optional<ProjectIssueModel> optionalSource = alertIssueModel.getSource();
            if (optionalSource.isPresent()) {
                ProjectIssueModel alertIssueSource = optionalSource.get();
                JiraIssueSearchProperties searchProperties = createSearchProperties(alertIssueSource);
                issuePropertiesManager.assignIssueProperties(issueKey, searchProperties);

                // FIXME figure out if alertIssueOrigin is required
                issueOrigin = createIssueOrigin(alertIssueSource);
            }

            addComments(issueKey, List.of("This issue was automatically created by Alert."));
            addComments(issueKey, alertIssueModel.getPostCreateComments());
        } catch (IntegrationRestException restException) {
            throw jiraErrorMessageUtility.improveRestException(restException, JiraCloudDescriptor.KEY_ISSUE_CREATOR, creationRequest.getReporterEmail());
        } catch (IntegrationException e) {
            // FIXME this will always catch the previous exception
            throw new AlertException(e);
        }

        String issueCallbackLink = JiraCallbackUtils.createUILink(createdIssue);
        // FIXME get summary
        return new IssueTrackerIssueResponseModel(issueOrigin, createdIssue.getKey(), issueCallbackLink, null, IssueOperation.OPEN);
    }

    // TODO consider adding this at search time
    private JiraIssueSearchProperties createSearchProperties(ProjectIssueModel alertIssueSource) {
        LinkableItem provider = alertIssueSource.getProvider();
        LinkableItem project = alertIssueSource.getProject();

        LinkableItem projectVersion = alertIssueSource.getProjectVersion()
                                          .orElseThrow(() -> new AlertRuntimeException("Missing project version"));

        BomComponentDetails bomComponent = alertIssueSource.getBomComponent();
        LinkableItem component = bomComponent.getComponent();
        String componentVersionLabel = bomComponent.getComponentVersion().map(LinkableItem::getLabel).orElse(null);
        String componentVersionName = bomComponent.getComponentVersion().map(LinkableItem::getValue).orElse(null);

        ComponentConcern arbitraryComponentConcern = bomComponent.getComponentConcerns()
                                                         .stream()
                                                         .findAny()
                                                         .orElseThrow(() -> new AlertRuntimeException("Missing component-concern"));
        ComponentConcernType concernType = arbitraryComponentConcern.getType();
        // TODO abstract this String construction also done in JqlStringCreator
        String category = StringUtils.capitalize(concernType.name().toLowerCase());

        String additionalKey = null;
        if (ComponentConcernType.POLICY.equals(concernType)) {
            // TODO abstract this String construction also done in JqlStringCreator
            additionalKey = String.format("Policy Violated%s", arbitraryComponentConcern.getName());
        }

        return new JiraIssueSearchProperties(
            provider.getLabel(),
            provider.getUrl().orElse(null),
            project.getLabel(),
            project.getValue(),
            projectVersion.getLabel(),
            projectVersion.getValue(),
            category,
            component.getLabel(),
            component.getValue(),
            componentVersionLabel,
            componentVersionName,
            additionalKey
        );
    }

    private void addComments(String issueKey, Collection<String> groupedComments) throws AlertException {
        for (String comment : groupedComments) {
            IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, comment);
            try {
                issueService.addComment(issueCommentRequestModel);
            } catch (IntegrationException e) {
                throw new AlertException("Failed to add a comment in Jira", e);
            }
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
