/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.search;

import java.util.Objects;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.blackduck.integration.alert.channel.azure.boards.distribution.util.AzureBoardsUILinkUtils;

public class AzureBoardsExistingIssueDetailsCreator {
    private final String organizationName;
    private final IssueCategoryRetriever issueCategoryRetriever;
    private final AzureBoardsIssueStatusResolver issueStatusResolver;

    public AzureBoardsExistingIssueDetailsCreator(String organizationName, IssueCategoryRetriever issueCategoryRetriever, AzureBoardsIssueStatusResolver issueStatusResolver) {
        this.organizationName = organizationName;
        this.issueCategoryRetriever = issueCategoryRetriever;
        this.issueStatusResolver = issueStatusResolver;
    }

    public ExistingIssueDetails<Integer> createIssueDetails(WorkItemResponseModel workItem, WorkItemFieldsWrapper workItemFields, ProjectIssueModel projectIssueModel) {
        Integer workItemId = workItem.getId();
        String workItemTitle = workItemFields.getField(WorkItemResponseFields.System_Title).orElse("Unknown Title");
        String workItemUILink = AzureBoardsUILinkUtils.extractUILink(organizationName, workItem);

        IssueCategory issueCategory = issueCategoryRetriever.retrieveIssueCategoryFromProjectIssueModel(projectIssueModel);
        String workItemState = workItemFields.getField(WorkItemResponseFields.System_State).orElse("Unknown");
        IssueStatus issueStatus = issueStatusResolver.resolveIssueStatus(workItemState);
        return new ExistingIssueDetails<>(workItemId, Objects.toString(workItemId), workItemTitle, workItemUILink, issueStatus, issueCategory);
    }

}
