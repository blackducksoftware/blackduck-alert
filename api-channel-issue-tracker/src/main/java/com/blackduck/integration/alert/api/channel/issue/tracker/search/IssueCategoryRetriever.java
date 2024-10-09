/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.search;

import java.util.function.BooleanSupplier;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;

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
