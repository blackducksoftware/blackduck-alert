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
import java.util.stream.Collectors;

import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public final class ProjectMessageToIssueModelTransformer {
    public static List<ProjectIssueModel> convertToIssueModels(ProjectMessage projectMessage) {
        return projectMessage.getBomComponents()
                   .stream()
                   .map(ProjectMessageToIssueModelTransformer::flattenToSingleConcernType)
                   .flatMap(List::stream)
                   .map(flattenedDetails -> copyFromProjectMessage(projectMessage, flattenedDetails))
                   .collect(Collectors.toList());

    }

    private static List<BomComponentDetails> flattenToSingleConcernType(BomComponentDetails bomComponentDetails) {
        List<ComponentConcern> componentConcerns = bomComponentDetails.getComponentConcerns();

        List<ComponentConcern> policyConcerns = new LinkedList<>();
        List<ComponentConcern> vulnerabilityConcerns = new LinkedList<>();

        for (ComponentConcern componentConcern : componentConcerns) {
            if (ComponentConcernType.POLICY.equals(componentConcern.getType())) {
                policyConcerns.add(componentConcern);
            } else {
                vulnerabilityConcerns.add(componentConcern);
            }
        }

        List<BomComponentDetails> flattenedDetails = new LinkedList<>();

        policyConcerns
            .stream()
            .map(concern -> copyAndReplaceConcerns(bomComponentDetails, List.of(concern)))
            .forEach(flattenedDetails::add);

        if (!vulnerabilityConcerns.isEmpty()) {
            BomComponentDetails flattenedVulnerabilityConcern = copyAndReplaceConcerns(bomComponentDetails, vulnerabilityConcerns);
            flattenedDetails.add(flattenedVulnerabilityConcern);
        }

        return flattenedDetails;
    }

    private static ProjectIssueModel copyFromProjectMessage(ProjectMessage projectMessage, BomComponentDetails bomComponent) {
        return new ProjectIssueModel(
            projectMessage.getProviderDetails(),
            projectMessage.getProject(),
            projectMessage.getProjectVersion().orElse(ProjectIssueModelConverter.MISSING_PROJECT_VERSION_PLACEHOLDER),
            bomComponent
        );
    }

    private static BomComponentDetails copyAndReplaceConcerns(BomComponentDetails oldComponent, List<ComponentConcern> componentConcerns) {
        return new BomComponentDetails(
            oldComponent.getComponent(),
            oldComponent.getComponentVersion().orElse(null),
            componentConcerns,
            oldComponent.getLicense(),
            oldComponent.getUsage(),
            oldComponent.getAdditionalAttributes(),
            oldComponent.getBlackDuckIssuesUrl()
        );
    }

    private ProjectMessageToIssueModelTransformer() {
    }

}
