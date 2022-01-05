/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.search;

import java.util.function.BooleanSupplier;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;

@Component
public class IssueCategoryRetriever {
    public IssueCategory retrieveIssueCategoryFromProjectIssueModel(ProjectIssueModel projectIssueModel) {
        return getIssueCategory(
            () -> projectIssueModel.getVulnerabilityDetails().isPresent(),
            () -> projectIssueModel.getPolicyDetails().isPresent());
    }

    public IssueCategory retrieveIssueCategoryFromComponentConcernType(ComponentConcernType componentConcernType) {
        return getIssueCategory(
            () -> ComponentConcernType.VULNERABILITY.equals(componentConcernType),
            () -> ComponentConcernType.POLICY.equals(componentConcernType));
    }

    private IssueCategory getIssueCategory(BooleanSupplier vulnerability, BooleanSupplier policy) {
        IssueCategory issueCategory = IssueCategory.BOM;
        if (vulnerability.getAsBoolean()) {
            issueCategory = IssueCategory.VULNERABILITY;
        } else if (policy.getAsBoolean()) {
            issueCategory = IssueCategory.POLICY;
        }
        return issueCategory;
    }

}
