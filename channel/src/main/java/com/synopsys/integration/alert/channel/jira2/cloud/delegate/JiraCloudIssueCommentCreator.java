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
package com.synopsys.integration.alert.channel.jira2.cloud.delegate;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.jira2.common.AlertJiraIssueOriginCreator;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.model.request.IssueCommentRequestModel;
import com.synopys.integration.alert.channel.api.issue.IssueTrackerIssueCommentCreator;
import com.synopys.integration.alert.channel.api.issue.model.ExistingIssueDetails;
import com.synopys.integration.alert.channel.api.issue.model.IssueCommentModel;

public class JiraCloudIssueCommentCreator implements IssueTrackerIssueCommentCreator<String> {
    private static final String COMMENTING_DISABLED_MESSAGE = "Commenting on issues is disabled. Skipping.";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JiraCloudJobDetailsModel distributionDetails;
    private final IssueService issueService;
    private final AlertJiraIssueOriginCreator alertJiraIssueOriginCreator;

    public JiraCloudIssueCommentCreator(JiraCloudJobDetailsModel distributionDetails, IssueService issueService, AlertJiraIssueOriginCreator alertJiraIssueOriginCreator) {
        this.distributionDetails = distributionDetails;
        this.issueService = issueService;
        this.alertJiraIssueOriginCreator = alertJiraIssueOriginCreator;
    }

    @Override
    public List<IssueTrackerIssueResponseModel> commentOnIssues(List<IssueCommentModel<String>> issueCommentModels) throws AlertException {
        if (!distributionDetails.isAddComments()) {
            logger.debug(COMMENTING_DISABLED_MESSAGE);
            return List.of();
        }

        List<IssueTrackerIssueResponseModel> responses = new LinkedList<>();
        for (IssueCommentModel<String> issueCommentModel : issueCommentModels) {
            ExistingIssueDetails<String> existingIssueDetails = issueCommentModel.getExistingIssueDetails();
            addComments(existingIssueDetails.getIssueKey(), issueCommentModel.getComments());

            AlertIssueOrigin alertIssueOrigin = alertJiraIssueOriginCreator.createIssueOrigin(issueCommentModel.getSource());
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

    public void addComment(String issueKey, String comment) throws AlertException {
        addComments(issueKey, List.of(comment));
    }

    public void addComments(String issueKey, List<String> groupedComments) throws AlertException {
        if (!distributionDetails.isAddComments()) {
            logger.debug(COMMENTING_DISABLED_MESSAGE);
            return;
        }

        for (String comment : groupedComments) {
            IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, comment);
            try {
                issueService.addComment(issueCommentRequestModel);
            } catch (IntegrationException e) {
                throw new AlertException("Failed to add a comment in Jira", e);
            }
        }
    }

}
