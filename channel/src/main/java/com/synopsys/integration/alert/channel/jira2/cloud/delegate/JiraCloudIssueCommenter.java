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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.api.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.model.request.IssueCommentRequestModel;

public class JiraCloudIssueCommenter extends IssueTrackerIssueCommenter<String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JiraCloudJobDetailsModel distributionDetails;
    private final IssueService issueService;

    public JiraCloudIssueCommenter(IssueTrackerIssueResponseCreator<String> issueResponseCreator, JiraCloudJobDetailsModel distributionDetails, IssueService issueService) {
        super(issueResponseCreator);
        this.distributionDetails = distributionDetails;
        this.issueService = issueService;
    }

    public void addComment(String issueKey, String comment) throws AlertException {
        addComments(issueKey, List.of(comment));
    }

    @Override
    public void addComments(IssueCommentModel<String> issueCommentModel) throws AlertException {
        ExistingIssueDetails<String> existingIssueDetails = issueCommentModel.getExistingIssueDetails();
        String issueKey = existingIssueDetails.getIssueKey();
        addComments(issueKey, issueCommentModel.getComments());
    }

    public void addComments(String issueKey, List<String> comments) throws AlertException {
        if (!isCommentingEnabled()) {
            logger.debug(COMMENTING_DISABLED_MESSAGE);
            return;
        }

        for (String comment : comments) {
            IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, comment);
            try {
                issueService.addComment(issueCommentRequestModel);
            } catch (IntegrationException e) {
                throw new AlertException(String.format("Failed to add a comment in Jira. Issue Key: %s", issueKey), e);
            }
        }
    }

    @Override
    protected boolean isCommentingEnabled() {
        return distributionDetails.isAddComments();
    }

}
