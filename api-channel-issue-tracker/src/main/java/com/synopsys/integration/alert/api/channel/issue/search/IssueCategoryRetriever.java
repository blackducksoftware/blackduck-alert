package com.synopsys.integration.alert.api.channel.issue.search;

import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;

public class IssueCategoryRetriever {
    public static IssueCategory retrieveIssueCategoryFromProjectIssueModel(ProjectIssueModel projectIssueModel) {
        IssueCategory issueCategory = IssueCategory.BOM;
        if (projectIssueModel.getVulnerabilityDetails().isPresent()) {
            issueCategory = IssueCategory.VULNERABILITY;
        } else if (projectIssueModel.getPolicyDetails().isPresent()) {
            issueCategory = IssueCategory.POLICY;
        }
        return issueCategory;
    }

    public static IssueCategory retrieveIssueCategoryFromComponentConcernType(ComponentConcernType componentConcernType) {
        IssueCategory issueCategory = IssueCategory.BOM;
        if (componentConcernType.equals(ComponentConcernType.VULNERABILITY)) {
            issueCategory = IssueCategory.VULNERABILITY;
        } else if (componentConcernType.equals(ComponentConcernType.POLICY)) {
            issueCategory = IssueCategory.POLICY;
        }
        return issueCategory;
    }
}
