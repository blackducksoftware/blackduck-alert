/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.ListUtils;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class ProcessedProviderMessageHolder extends AlertSerializableModel {
    private final List<ProcessedProviderMessage<ProjectMessage>> processedProjectMessages;
    private final List<ProcessedProviderMessage<SimpleMessage>> processedSimpleMessages;

    public static ProcessedProviderMessageHolder reduce(ProcessedProviderMessageHolder lhs, ProcessedProviderMessageHolder rhs) {
        List<ProcessedProviderMessage<ProjectMessage>> unifiedProjectMessages = ListUtils.union(lhs.getProcessedProjectMessages(), rhs.getProcessedProjectMessages());
        List<ProcessedProviderMessage<SimpleMessage>> unifiedSimpleMessages = ListUtils.union(lhs.getProcessedSimpleMessages(), rhs.getProcessedSimpleMessages());
        return new ProcessedProviderMessageHolder(unifiedProjectMessages, unifiedSimpleMessages);
    }

    public static ProcessedProviderMessageHolder empty() {
        return new ProcessedProviderMessageHolder(List.of(), List.of());
    }

    public ProcessedProviderMessageHolder(
        List<ProcessedProviderMessage<ProjectMessage>> processedProjectMessages,
        List<ProcessedProviderMessage<SimpleMessage>> processedSimpleMessages
    ) {
        this.processedProjectMessages = processedProjectMessages;
        this.processedSimpleMessages = processedSimpleMessages;
    }

    public List<ProcessedProviderMessageHolder> expand() {
        List<ProcessedProviderMessageHolder> singleMessageHolders = new ArrayList<>(processedProjectMessages.size() + processedSimpleMessages.size());

        for (ProcessedProviderMessage<ProjectMessage> processedProjectMessage : processedProjectMessages) {
            ProcessedProviderMessageHolder singleProjectMessageHolder = new ProcessedProviderMessageHolder(List.of(processedProjectMessage), List.of());
            singleMessageHolders.add(singleProjectMessageHolder);
        }

        for (ProcessedProviderMessage<SimpleMessage> processedSimpleMessage : processedSimpleMessages) {
            ProcessedProviderMessageHolder singleSimpleMessageHolder = new ProcessedProviderMessageHolder(List.of(), List.of(processedSimpleMessage));
            singleMessageHolders.add(singleSimpleMessageHolder);
        }

        return singleMessageHolders;
    }

    public ProviderMessageHolder toProviderMessageHolder() {
        List<ProjectMessage> projectMessages = extractProviderMessages(processedProjectMessages);
        List<SimpleMessage> simpleMessages = extractProviderMessages(processedSimpleMessages);
        return new ProviderMessageHolder(projectMessages, simpleMessages);
    }

    public Set<Long> extractAllNotificationIds() {
        return Stream.concat(processedProjectMessages.stream(), processedSimpleMessages.stream())
                   .map(ProcessedProviderMessage::getNotificationIds)
                   .flatMap(Set::stream)
                   .collect(Collectors.toSet());
    }

    public List<ProcessedProviderMessage<ProjectMessage>> getProcessedProjectMessages() {
        return processedProjectMessages;
    }

    public List<ProcessedProviderMessage<SimpleMessage>> getProcessedSimpleMessages() {
        return processedSimpleMessages;
    }

    private <T extends ProviderMessage<T>> List<T> extractProviderMessages(List<ProcessedProviderMessage<T>> processedMessages) {
        return processedMessages
                   .stream()
                   .map(ProcessedProviderMessage::getProviderMessage)
                   .collect(Collectors.toList());
    }

}
