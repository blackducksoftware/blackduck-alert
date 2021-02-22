/*
 * processor-api
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
package com.synopsys.integration.alert.processor.api.summarize;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.alert.processor.api.extract.model.project.MessageReason;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

@Component
public class ProjectMessageSummarizer {
    public SimpleMessage summarize(ProjectMessage digestedProjectMessage) {
        Pair<String, String> summaryAndDescription = constructSummaryAndDescription(digestedProjectMessage);
        List<LinkableItem> details = constructMessageDetails(digestedProjectMessage);
        return SimpleMessage.derived(summaryAndDescription.getLeft(), summaryAndDescription.getRight(), details, digestedProjectMessage);
    }

    private Pair<String, String> constructSummaryAndDescription(ProjectMessage projectMessage) {
        ProviderDetails providerDetails = projectMessage.getProviderDetails();
        String providerName = providerDetails.getProvider().getValue();
        String projectName = projectMessage.getProject().getValue();

        MessageReason messageReason = projectMessage.getMessageReason();
        switch (messageReason) {
            case PROJECT_STATUS:
            case PROJECT_VERSION_STATUS:
                return constructProjectVersionStatusSummaryAndDescription(providerName, projectName, projectMessage);
            case COMPONENT_UPDATE:
            case COMPONENT_CONCERN:
                return constructComponentStatusAndDescription(providerName, projectName, messageReason, projectMessage);
            default:
                return Pair.of(
                    String.format("%s had notifications in BlackDuck", projectMessage.getProject().getValue()),
                    "Alert received an unrecognized notification from BlackDuck."
                );
        }
    }

    private Pair<String, String> constructProjectVersionStatusSummaryAndDescription(String providerName, String projectName, ProjectMessage projectMessage) {
        String operationString = projectMessage.getOperation()
                                     .map(this::convertToParticiple)
                                     .orElse("updated");

        Optional<String> optionalProjectVersionName = projectMessage.getProjectVersion().map(LinkableItem::getValue);
        if (optionalProjectVersionName.isPresent()) {
            String projectVersionName = optionalProjectVersionName.get();
            return Pair.of(
                String.format("[%s] %s > %s %s", providerName, projectName, projectVersionName, operationString),
                String.format("A project-version '%s' was %s was in the BlackDuck project '%s'.", projectVersionName, operationString, projectName)
            );
        } else {
            return Pair.of(
                String.format("[%s] %s %s", providerName, projectName, operationString),
                String.format("The project '%s' was %s in BlackDuck.", projectName, operationString)
            );
        }
    }

    private Pair<String, String> constructComponentStatusAndDescription(String providerName, String projectName, MessageReason messageReason, ProjectMessage projectMessage) {
        String projectVersionName = projectMessage.getProjectVersion()
                                        .map(LinkableItem::getValue)
                                        .orElse("Unknown Version");
        String operationString = MessageReason.COMPONENT_CONCERN.equals(messageReason) ? "problems" : "updates";
        return Pair.of(
            String.format("[%s] %s > %s component %s", providerName, projectName, projectVersionName, operationString),
            String.format("The project-version '%s > %s' had component %s in BlackDuck", projectName, projectVersionName, operationString)
        );
    }

    private List<LinkableItem> constructMessageDetails(ProjectMessage projectMessage) {
        List<LinkableItem> details = new LinkedList<>();
        details.add(projectMessage.getProject());
        projectMessage.getProjectVersion().ifPresent(details::add);

        Map<ComponentConcernSummaryGrouping, Integer> groupedConcernCounts = new LinkedHashMap<>();
        for (BomComponentDetails bomComponent : projectMessage.getBomComponents()) {
            for (ComponentConcern concern : bomComponent.getComponentConcerns()) {
                ComponentConcernSummaryGrouping concernKey = new ComponentConcernSummaryGrouping(concern.getType(), concern.getOperation(), concern.getSeverity());
                int currentCount = groupedConcernCounts.getOrDefault(concernKey, 0);
                groupedConcernCounts.put(concernKey, currentCount + 1);
            }
        }

        for (Map.Entry<ComponentConcernSummaryGrouping, Integer> groupedConcernCount : groupedConcernCounts.entrySet()) {
            ComponentConcernSummaryGrouping concernGrouping = groupedConcernCount.getKey();

            String stateTypeString = convertToUppercasePlural(concernGrouping.type);
            String stateAdjective = convertToAdjective(concernGrouping.type, concernGrouping.operation);

            String label = String.format("(%s) %s %s count:", concernGrouping.severity, stateTypeString, stateAdjective);
            LinkableItem concernDetail = new LinkableItem(label, groupedConcernCount.getValue().toString());
            concernDetail.setNumericValueFlag(true);
            details.add(concernDetail);
        }

        return details;
    }

    private String convertToParticiple(ProjectOperation projectOperation) {
        switch (projectOperation) {
            case CREATE:
                return "created";
            case DELETE:
                return "deleted";
            default:
                return "updated";
        }
    }

    private String convertToUppercasePlural(ComponentConcernType type) {
        switch (type) {
            case POLICY:
                return "Policies";
            case VULNERABILITY:
                return "Vulnerabilities";
            default:
                return "Concerns";
        }
    }

    private String convertToAdjective(ComponentConcernType type, ItemOperation operation) {
        switch (operation) {
            case ADD:
                return ComponentConcernType.POLICY.equals(type) ? "violated" : "added";
            case DELETE:
                return ComponentConcernType.POLICY.equals(type) ? "no longer in violation" : "deleted";
            case UPDATE:
                return "updated";
            default:
                return "with other component updates";
        }
    }

    private static class ComponentConcernSummaryGrouping extends AlertSerializableModel {
        private final ComponentConcernType type;
        private final ItemOperation operation;
        private final ComponentConcernSeverity severity;

        public ComponentConcernSummaryGrouping(ComponentConcernType type, ItemOperation operation, ComponentConcernSeverity severity) {
            this.type = type;
            this.operation = operation;
            this.severity = severity;
        }

    }

}
