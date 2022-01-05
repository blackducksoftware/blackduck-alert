/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.model;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;

public class ProjectIssueModel extends ProviderMessage<ProjectIssueModel> {
    private final LinkableItem project;
    private final LinkableItem projectVersion;
    private final IssueBomComponentDetails bomComponentDetails;

    private final IssuePolicyDetails policyDetails;
    private final IssueVulnerabilityDetails vulnerabilityDetails;
    private final IssueComponentUnknownVersionDetails componentUnknownVersionDetails;

    public static ProjectIssueModel bom(
        ProviderDetails providerDetails,
        LinkableItem project,
        LinkableItem projectVersion,
        IssueBomComponentDetails bomComponentDetails
    ) {
        return new ProjectIssueModel(providerDetails, project, projectVersion, bomComponentDetails, null, null, null);
    }

    public static ProjectIssueModel policy(
        ProviderDetails providerDetails,
        LinkableItem project,
        LinkableItem projectVersion,
        IssueBomComponentDetails bomComponentDetails,
        IssuePolicyDetails policyDetails
    ) {
        return new ProjectIssueModel(providerDetails, project, projectVersion, bomComponentDetails, policyDetails, null, null);
    }

    public static ProjectIssueModel vulnerability(
        ProviderDetails providerDetails,
        LinkableItem project,
        LinkableItem projectVersion,
        IssueBomComponentDetails bomComponentDetails,
        IssueVulnerabilityDetails vulnerabilityDetails
    ) {
        return new ProjectIssueModel(providerDetails, project, projectVersion, bomComponentDetails, null, vulnerabilityDetails, null);
    }

    public static ProjectIssueModel componentUnknownVersion(
        ProviderDetails providerDetails,
        LinkableItem project,
        LinkableItem projectVersion,
        IssueBomComponentDetails bomComponentDetails,
        IssueComponentUnknownVersionDetails unknownVersionDetails
    ) {
        return new ProjectIssueModel(providerDetails, project, projectVersion, bomComponentDetails, null, null, unknownVersionDetails);
    }

    private ProjectIssueModel(
        ProviderDetails providerDetails,
        LinkableItem project,
        LinkableItem projectVersion,
        IssueBomComponentDetails bomComponentDetails,
        @Nullable IssuePolicyDetails policyDetails,
        @Nullable IssueVulnerabilityDetails vulnerabilityDetails,
        @Nullable IssueComponentUnknownVersionDetails componentUnknownVersionDetails
    ) {
        super(providerDetails);
        this.project = project;
        this.projectVersion = projectVersion;
        this.bomComponentDetails = bomComponentDetails;
        this.policyDetails = policyDetails;
        this.vulnerabilityDetails = vulnerabilityDetails;
        this.componentUnknownVersionDetails = componentUnknownVersionDetails;
    }

    public LinkableItem getProject() {
        return project;
    }

    public Optional<LinkableItem> getProjectVersion() {
        return Optional.ofNullable(projectVersion);
    }

    public IssueBomComponentDetails getBomComponentDetails() {
        return bomComponentDetails;
    }

    public Optional<IssuePolicyDetails> getPolicyDetails() {
        return Optional.ofNullable(policyDetails);
    }

    public Optional<IssueVulnerabilityDetails> getVulnerabilityDetails() {
        return Optional.ofNullable(vulnerabilityDetails);
    }

    public Optional<IssueComponentUnknownVersionDetails> getComponentUnknownVersionDetails() {
        return Optional.ofNullable(componentUnknownVersionDetails);
    }

    @Override
    public List<ProjectIssueModel> combine(ProjectIssueModel otherModel) {
        List<ProjectIssueModel> uncombinedModels = List.of(this, otherModel);

        if (!project.equals(otherModel.project)) {
            return uncombinedModels;
        }

        if (null != projectVersion && !projectVersion.equals(otherModel.projectVersion)) {
            return uncombinedModels;
        }

        if (!bomComponentDetails.equals(otherModel.bomComponentDetails)) {
            return uncombinedModels;
        }

        if (null != policyDetails && !policyDetails.equals(otherModel.policyDetails)) {
            return uncombinedModels;
        }

        if (null != vulnerabilityDetails && !vulnerabilityDetails.equals(otherModel.vulnerabilityDetails)) {
            return uncombinedModels;
        }

        if (null != componentUnknownVersionDetails && !componentUnknownVersionDetails.equals(otherModel.componentUnknownVersionDetails)) {
            return uncombinedModels;
        }

        return List.of(this);
    }

}
