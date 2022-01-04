/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.convert;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueComponentUnknownVersionDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueEstimatedRiskModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueVulnerabilityDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueVulnerabilityModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

@Component
public class ProjectMessageToIssueModelTransformer {
    public List<ProjectIssueModel> convertToIssueModels(ProjectMessage projectMessage) {
        List<ProjectIssueModel> projectIssueModels = new LinkedList<>();
        for (BomComponentDetails bomComponent : projectMessage.getBomComponents()) {
            IssueBomComponentDetails issueBomComponent = IssueBomComponentDetails.fromBomComponentDetails(bomComponent);
            List<ProjectIssueModel> projectIssueModelsForConcerns = convertToIssueModels(projectMessage, issueBomComponent, bomComponent.getComponentConcerns());
            projectIssueModels.addAll(projectIssueModelsForConcerns);
        }
        return projectIssueModels;
    }

    private List<ProjectIssueModel> convertToIssueModels(ProjectMessage projectMessage, IssueBomComponentDetails issueBomComponent, List<ComponentConcern> componentConcerns) {
        List<ComponentConcern> policyConcerns = new LinkedList<>();
        List<ComponentConcern> vulnerabilityConcerns = new LinkedList<>();
        List<ComponentConcern> estimatedRiskConcerns = new LinkedList<>();

        for (ComponentConcern componentConcern : componentConcerns) {
            if (ComponentConcernType.POLICY.equals(componentConcern.getType())) {
                policyConcerns.add(componentConcern);
            } else if (ComponentConcernType.UNKNOWN_VERSION.equals(componentConcern.getType())) {
                estimatedRiskConcerns.add(componentConcern);
            } else {
                vulnerabilityConcerns.add(componentConcern);
            }
        }

        List<ProjectIssueModel> projectIssueModels = new LinkedList<>();

        policyConcerns
            .stream()
            .map(concern -> createPolicyProjectIssueModel(projectMessage, issueBomComponent, concern))
            .forEach(projectIssueModels::add);

        if (!vulnerabilityConcerns.isEmpty()) {
            ProjectIssueModel vulnerabilityProjectIssueModel = createVulnerabilityProjectIssueModel(projectMessage, issueBomComponent, vulnerabilityConcerns);
            projectIssueModels.add(vulnerabilityProjectIssueModel);
        }

        if (!estimatedRiskConcerns.isEmpty()) {
            ProjectIssueModel estimatedRiskProjectIssueModel = createEstimatedRiskProjectIssueModel(projectMessage, issueBomComponent, estimatedRiskConcerns);
            projectIssueModels.add(estimatedRiskProjectIssueModel);
        }

        return projectIssueModels;
    }

    private static ProjectIssueModel createPolicyProjectIssueModel(ProjectMessage projectMessage, IssueBomComponentDetails issueBomComponent, ComponentConcern policyConcern) {
        IssuePolicyDetails policyDetails = new IssuePolicyDetails(policyConcern.getName(), policyConcern.getOperation(), policyConcern.getSeverity());
        return ProjectIssueModel.policy(
            projectMessage.getProviderDetails(),
            projectMessage.getProject(),
            projectMessage.getProjectVersion().orElse(null),
            issueBomComponent,
            policyDetails
        );
    }

    private ProjectIssueModel createVulnerabilityProjectIssueModel(ProjectMessage projectMessage, IssueBomComponentDetails issueBomComponent, List<ComponentConcern> vulnerabilityConcerns) {
        List<IssueVulnerabilityModel> issueVulnerabilitiesAdded = new LinkedList<>();
        List<IssueVulnerabilityModel> issueVulnerabilitiesUpdated = new LinkedList<>();
        List<IssueVulnerabilityModel> issueVulnerabilitiesDeleted = new LinkedList<>();

        for (ComponentConcern vulnerabilityConcern : vulnerabilityConcerns) {
            IssueVulnerabilityModel issueVulnerabilityModel = IssueVulnerabilityModel.fromComponentConcern(vulnerabilityConcern);

            ItemOperation vulnOperation = vulnerabilityConcern.getOperation();
            if (ItemOperation.ADD.equals(vulnOperation)) {
                issueVulnerabilitiesAdded.add(issueVulnerabilityModel);
            } else if (ItemOperation.DELETE.equals(vulnOperation)) {
                issueVulnerabilitiesDeleted.add(issueVulnerabilityModel);
            } else {
                issueVulnerabilitiesUpdated.add(issueVulnerabilityModel);
            }
        }

        ComponentVulnerabilities componentVulnerabilities = issueBomComponent.getComponentVulnerabilities();
        IssueVulnerabilityDetails vulnerabilityDetails = new IssueVulnerabilityDetails(
            !componentVulnerabilities.hasVulnerabilities(),
            issueVulnerabilitiesAdded,
            issueVulnerabilitiesUpdated,
            issueVulnerabilitiesDeleted
        );
        return ProjectIssueModel.vulnerability(
            projectMessage.getProviderDetails(),
            projectMessage.getProject(),
            projectMessage.getProjectVersion().orElse(null),
            issueBomComponent,
            vulnerabilityDetails
        );
    }

    private ProjectIssueModel createEstimatedRiskProjectIssueModel(ProjectMessage projectMessage, IssueBomComponentDetails issueBomComponent, List<ComponentConcern> estimatedRiskConcerns) {
        List<IssueEstimatedRiskModel> estimatedRiskModels = new LinkedList<>();

        ItemOperation itemOperation = estimatedRiskConcerns.stream()
            .map(ComponentConcern::getOperation)
            .findFirst()
            .orElse(ItemOperation.ADD);
        // all component concerns for this type have the same operation.
        for (ComponentConcern componentConcern : estimatedRiskConcerns) {
            estimatedRiskModels.add(IssueEstimatedRiskModel.fromComponentConcern(componentConcern));
        }

        IssueComponentUnknownVersionDetails unknownVersionDetails = new IssueComponentUnknownVersionDetails(itemOperation, estimatedRiskModels);

        return ProjectIssueModel.componentUnknownVersion(
            projectMessage.getProviderDetails(),
            projectMessage.getProject(),
            projectMessage.getProjectVersion().orElse(null),
            issueBomComponent,
            unknownVersionDetails
        );
    }

}
