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
package com.synopsys.integration.alert.channel.azureboards2.delegate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.api.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.service.comment.AzureWorkItemCommentService;

public class AzureBoardsIssueCommenter extends IssueTrackerIssueCommenter<Integer> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String organizationName;
    private final AzureBoardsJobDetailsModel distributionDetails;
    private final AzureWorkItemCommentService commentService;

    public AzureBoardsIssueCommenter(
        IssueTrackerIssueResponseCreator<Integer> issueResponseCreator,
        String organizationName,
        AzureBoardsJobDetailsModel distributionDetails,
        AzureWorkItemCommentService commentService
    ) {
        super(issueResponseCreator);
        this.organizationName = organizationName;
        this.distributionDetails = distributionDetails;
        this.commentService = commentService;
    }

    @Override
    protected boolean isCommentingEnabled() {
        return distributionDetails.isAddComments();
    }

    // TODO abstract this method more
    @Override
    protected void addComments(IssueCommentModel<Integer> issueCommentModel) throws AlertException {
        if (!isCommentingEnabled()) {
            logger.debug(COMMENTING_DISABLED_MESSAGE);
            return;
        }

        ExistingIssueDetails<Integer> existingIssueDetails = issueCommentModel.getExistingIssueDetails();
        for (String comment : issueCommentModel.getComments()) {
            try {
                commentService.addComment(organizationName, distributionDetails.getProjectNameOrId(), existingIssueDetails.getIssueId(), comment);
            } catch (HttpServiceException e) {
                throw new AlertException(String.format("Failed to add Azure Boards comment. Issue Key: %s", existingIssueDetails.getIssueId()), e);
            }
        }
    }

}
