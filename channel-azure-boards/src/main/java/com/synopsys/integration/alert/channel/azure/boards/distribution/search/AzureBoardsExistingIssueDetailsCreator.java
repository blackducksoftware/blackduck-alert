/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import java.util.Objects;

import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsUILinkUtils;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;

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
