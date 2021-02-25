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
package com.synopsys.integration.alert.channel.api.issue.model;

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

    public static ProjectIssueModel bom(
        ProviderDetails providerDetails,
        LinkableItem project,
        LinkableItem projectVersion,
        IssueBomComponentDetails bomComponentDetails
    ) {
        return new ProjectIssueModel(providerDetails, project, projectVersion, bomComponentDetails, null, null);
    }

    public static ProjectIssueModel policy(
        ProviderDetails providerDetails,
        LinkableItem project,
        LinkableItem projectVersion,
        IssueBomComponentDetails bomComponentDetails,
        IssuePolicyDetails policyDetails
    ) {
        return new ProjectIssueModel(providerDetails, project, projectVersion, bomComponentDetails, policyDetails, null);
    }

    public static ProjectIssueModel vulnerability(
        ProviderDetails providerDetails,
        LinkableItem project,
        LinkableItem projectVersion,
        IssueBomComponentDetails bomComponentDetails,
        IssueVulnerabilityDetails vulnerabilityDetails
    ) {
        return new ProjectIssueModel(providerDetails, project, projectVersion, bomComponentDetails, null, vulnerabilityDetails);
    }

    private ProjectIssueModel(
        ProviderDetails providerDetails,
        LinkableItem project,
        LinkableItem projectVersion,
        IssueBomComponentDetails bomComponentDetails,
        @Nullable IssuePolicyDetails policyDetails,
        @Nullable IssueVulnerabilityDetails vulnerabilityDetails
    ) {
        super(providerDetails);
        this.project = project;
        this.projectVersion = projectVersion;
        this.bomComponentDetails = bomComponentDetails;
        this.policyDetails = policyDetails;
        this.vulnerabilityDetails = vulnerabilityDetails;
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

        return List.of(this);
    }

}
