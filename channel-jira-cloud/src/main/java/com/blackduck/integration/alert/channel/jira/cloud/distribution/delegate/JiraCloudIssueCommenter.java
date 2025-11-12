/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.distribution.delegate;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueResponseCreator;
import com.blackduck.integration.alert.api.channel.jira.distribution.delegate.JiraIssueCommenter;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.cloud.model.AtlassianDocumentFormatModel;
import com.blackduck.integration.jira.common.cloud.service.IssueService;
import com.blackduck.integration.jira.common.cloud.model.IssueCommentRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class JiraCloudIssueCommenter extends JiraIssueCommenter<IssueCommentRequestModel> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IssueService issueService;
    private final JiraCloudJobDetailsModel distributionDetails;

    public JiraCloudIssueCommenter(IssueTrackerIssueResponseCreator issueResponseCreator, IssueService issueService, JiraCloudJobDetailsModel distributionDetails) {
        super(issueResponseCreator);
        this.issueService = issueService;
        this.distributionDetails = distributionDetails;
    }

    @Override
    protected boolean isCommentingEnabled() {
        return distributionDetails.isAddComments();
    }

    @Override
    protected void addComment(IssueCommentRequestModel requestModel) throws IntegrationException {
        issueService.addComment(requestModel);
    }

    @Override
    protected IssueCommentRequestModel createCommentModel(String comment, ExistingIssueDetails<String> existingIssueDetails) throws IntegrationException {
        return IssueCommentRequestModel.commentForIssue(existingIssueDetails.getIssueKey(), comment);
    }

    @Override
    protected void addComments(IssueCommentModel<String> issueCommentModel) throws AlertException {
        if (!isCommentingEnabled()) {
            logger.debug(COMMENTING_DISABLED_MESSAGE);
            return;
        }

        for (String comment : issueCommentModel.getComments()) {
            // need to create the model from the issue comment model which has the atlassian document format
            Optional<AtlassianDocumentFormatModel> primaryComment = issueCommentModel.getAtlassianDocumentFormatCommentModel();
            ExistingIssueDetails<String> existingIssueDetails = issueCommentModel.getExistingIssueDetails();
            if (primaryComment.isPresent()) {
               IssueCommentRequestModel commentRequestModel =  new IssueCommentRequestModel(existingIssueDetails.getIssueKey(),primaryComment.get());
                try {
                    addComment(commentRequestModel);

                    Optional<List<AtlassianDocumentFormatModel>> additionalComments = issueCommentModel.getAdditionalComments();
                    if (additionalComments.isPresent()) {
                        List<AtlassianDocumentFormatModel> additionalCommentsList = additionalComments.get();
                        for(AtlassianDocumentFormatModel additionalComment : additionalCommentsList) {
                            commentRequestModel = new IssueCommentRequestModel(existingIssueDetails.getIssueKey(), additionalComment);
                            addComment(commentRequestModel);
                        }
                    }

                } catch (IntegrationException ex) {
                    throw new AlertException(String.format("Failed to add a comment in Jira. Issue Key: %s", existingIssueDetails.getIssueKey()), ex);
                }
            } else {
                addComment(comment, issueCommentModel.getExistingIssueDetails(), issueCommentModel.getSource().orElse(null));
            }
        }
    }
}
