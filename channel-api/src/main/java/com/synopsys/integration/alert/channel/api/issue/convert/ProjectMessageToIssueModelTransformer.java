/*
 * channel-api
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
package com.synopsys.integration.alert.channel.api.issue.convert;

import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.alert.channel.api.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssueVulnerabilityDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssueVulnerabilityModel;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public final class ProjectMessageToIssueModelTransformer {
    public static List<ProjectIssueModel> convertToIssueModels(ProjectMessage projectMessage) {
        List<ProjectIssueModel> projectIssueModels = new LinkedList<>();
        for (BomComponentDetails bomComponent : projectMessage.getBomComponents()) {
            IssueBomComponentDetails issueBomComponent = IssueBomComponentDetails.fromBomComponentDetails(bomComponent);
            List<ProjectIssueModel> projectIssueModelsForConcerns = convertToIssueModels(projectMessage, issueBomComponent, bomComponent.getComponentConcerns());
            projectIssueModels.addAll(projectIssueModelsForConcerns);
        }
        return projectIssueModels;
    }

    private static List<ProjectIssueModel> convertToIssueModels(ProjectMessage projectMessage, IssueBomComponentDetails issueBomComponent, List<ComponentConcern> componentConcerns) {
        List<ComponentConcern> policyConcerns = new LinkedList<>();
        List<ComponentConcern> vulnerabilityConcerns = new LinkedList<>();

        for (ComponentConcern componentConcern : componentConcerns) {
            if (ComponentConcernType.POLICY.equals(componentConcern.getType())) {
                policyConcerns.add(componentConcern);
            } else {
                vulnerabilityConcerns.add(componentConcern);
            }
        }

        List<ProjectIssueModel> projectIssueModels = new LinkedList<>();

        policyConcerns
            .stream()
            .map(concern -> createPolicyProjectIssueModel(projectMessage, issueBomComponent, concern))
            .forEach(projectIssueModels::add);

        ProjectIssueModel vulnerabilityProjectIssueModel = createVulnerabilityProjectIssueModel(projectMessage, issueBomComponent, vulnerabilityConcerns);
        projectIssueModels.add(vulnerabilityProjectIssueModel);

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

    private static ProjectIssueModel createVulnerabilityProjectIssueModel(ProjectMessage projectMessage, IssueBomComponentDetails issueBomComponent, List<ComponentConcern> vulnerabilityConcerns) {
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

        IssueVulnerabilityDetails vulnerabilityDetails = new IssueVulnerabilityDetails(
            // TODO cary this information from the processing level
            false,
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

    private ProjectMessageToIssueModelTransformer() {
    }

}
