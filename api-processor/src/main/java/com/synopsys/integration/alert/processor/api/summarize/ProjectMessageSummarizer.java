/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.summarize;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessage;
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
    public static final String OP_PARTICIPLE_ADDED = "added";
    public static final String OP_PARTICIPLE_CREATED = "created";
    public static final String OP_PARTICIPLE_DELETED = "deleted";
    public static final String OP_PARTICIPLE_UPDATED = "updated";
    public static final String OP_PARTICIPLE_VIOLATED = "violated";

    public ProcessedProviderMessage<SimpleMessage> summarize(ProcessedProviderMessage<ProjectMessage> digestedProjectMessage) {
        ProjectMessage projectMessage = digestedProjectMessage.getProviderMessage();
        Pair<String, String> summaryAndDescription = constructSummaryAndDescription(projectMessage);
        List<LinkableItem> details = constructMessageDetails(projectMessage);
        SimpleMessage derivedSimpleMessage = SimpleMessage.derived(summaryAndDescription.getLeft(), summaryAndDescription.getRight(), details, projectMessage);
        return new ProcessedProviderMessage<>(digestedProjectMessage.getNotificationIds(), derivedSimpleMessage);
    }

    private Pair<String, String> constructSummaryAndDescription(ProjectMessage projectMessage) {
        String providerName = projectMessage.getProvider().getValue();
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
            .orElse(OP_PARTICIPLE_UPDATED);

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
                Number concernNumericValue = concern.getNumericValue();
                int concernCount = (concernNumericValue != null) ? concernNumericValue.intValue() : 1;
                groupedConcernCounts.put(concernKey, currentCount + concernCount);
            }
        }
        Map<ComponentConcernSummaryGrouping, Integer> sortedGroupedConcernCountsBySeverity = groupedConcernCounts
                                                                                                 .entrySet()
                                                                                                 .stream()
                                                                                                 .sorted(Map.Entry.comparingByKey(ComponentConcernSummaryGrouping.getComparator()))
                                                                                                 .collect(Collectors.toMap(
                                                                                                     Map.Entry::getKey,
                                                                                                     Map.Entry::getValue,
                                                                                                     (old, newIgnored) -> old, // Merge operation is equivalent to Map::putIfAbsent
                                                                                                     LinkedHashMap::new));

        for (Map.Entry<ComponentConcernSummaryGrouping, Integer> groupedConcernCount : sortedGroupedConcernCountsBySeverity.entrySet()) {
            ComponentConcernSummaryGrouping concernGrouping = groupedConcernCount.getKey();

            String severityLabel = ComponentConcernType.POLICY.equals(concernGrouping.type) ? concernGrouping.severity.getPolicyLabel() : concernGrouping.severity.getVulnerabilityLabel();
            String stateTypeString = convertToUppercasePlural(concernGrouping.type);
            String stateAdjective = convertToAdjective(concernGrouping.type, concernGrouping.operation);

            String label = String.format("(%s) %s %s count", severityLabel, stateTypeString, stateAdjective);
            LinkableItem concernDetail = new LinkableItem(label, groupedConcernCount.getValue().toString());
            concernDetail.setNumericValueFlag(true);
            details.add(concernDetail);
        }

        return details;
    }

    private String convertToParticiple(ProjectOperation projectOperation) {
        switch (projectOperation) {
            case CREATE:
                return OP_PARTICIPLE_CREATED;
            case DELETE:
                return OP_PARTICIPLE_DELETED;
            default:
                return OP_PARTICIPLE_UPDATED;
        }
    }

    private String convertToUppercasePlural(ComponentConcernType type) {
        switch (type) {
            case POLICY:
                return "Policies";
            case UNKNOWN_VERSION:
                return "Estimated Security Risks";
            case VULNERABILITY:
                return "Vulnerabilities";
            default:
                return "Concerns";
        }
    }

    private String convertToAdjective(ComponentConcernType type, ItemOperation operation) {
        switch (operation) {
            case ADD:
                return ComponentConcernType.POLICY.equals(type) ? OP_PARTICIPLE_VIOLATED : OP_PARTICIPLE_ADDED;
            case DELETE:
                return ComponentConcernType.POLICY.equals(type) ? "no longer in violation" : OP_PARTICIPLE_DELETED;
            case UPDATE:
                return OP_PARTICIPLE_UPDATED;
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

        public static Comparator<ComponentConcernSummaryGrouping> getComparator() {
            Comparator<ComponentConcernSummaryGrouping> comparatorType = Comparator.comparing(grouping -> grouping.type);
            Comparator<ComponentConcernSummaryGrouping> comparatorSeverity = Comparator.comparing(grouping -> grouping.severity);
            return comparatorType.thenComparing(comparatorSeverity);
        }
    }

}
