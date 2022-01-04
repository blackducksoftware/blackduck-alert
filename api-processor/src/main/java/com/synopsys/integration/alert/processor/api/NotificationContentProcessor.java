/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.processor.api.digest.ProjectMessageDigester;
import com.synopsys.integration.alert.processor.api.extract.ProviderMessageExtractionDelegator;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.processor.api.summarize.ProjectMessageSummarizer;

@Component
public class NotificationContentProcessor {
    private final ProviderMessageExtractionDelegator providerMessageExtractionDelegator;
    private final ProjectMessageDigester projectMessageDigester;
    private final ProjectMessageSummarizer projectMessageSummarizer;

    @Autowired
    public NotificationContentProcessor(
        ProviderMessageExtractionDelegator providerMessageExtractionDelegator,
        ProjectMessageDigester projectMessageDigester,
        ProjectMessageSummarizer projectMessageSummarizer
    ) {
        this.providerMessageExtractionDelegator = providerMessageExtractionDelegator;
        this.projectMessageDigester = projectMessageDigester;
        this.projectMessageSummarizer = projectMessageSummarizer;
    }

    public ProcessedProviderMessageHolder processNotificationContent(ProcessingType processingType, List<NotificationContentWrapper> jobNotifications) {
        ProcessedProviderMessageHolder extractedProviderMessages = jobNotifications
            .stream()
            .map(providerMessageExtractionDelegator::extract)
            .reduce(ProcessedProviderMessageHolder::reduce)
            .orElse(ProcessedProviderMessageHolder.empty());
        return processExtractedNotifications(processingType, extractedProviderMessages);
    }

    private ProcessedProviderMessageHolder processExtractedNotifications(ProcessingType processingType, ProcessedProviderMessageHolder providerMessages) {
        List<ProcessedProviderMessage<ProjectMessage>> filteredProjectMessages = filterProcessedMessages(providerMessages.getProcessedProjectMessages());
        List<ProcessedProviderMessage<SimpleMessage>> filteredSimpleMessages = filterProcessedMessages(providerMessages.getProcessedSimpleMessages());
        if (ProcessingType.DEFAULT.equals(processingType)) {
            return new ProcessedProviderMessageHolder(filteredProjectMessages, filteredSimpleMessages);
        }

        List<ProcessedProviderMessage<ProjectMessage>> digestedMessages = projectMessageDigester.digest(filteredProjectMessages);
        if (ProcessingType.SUMMARY.equals(processingType)) {
            List<ProcessedProviderMessage<SimpleMessage>> summarizedMessages = digestedMessages
                .stream()
                .map(projectMessageSummarizer::summarize)
                .collect(Collectors.toList());
            List<ProcessedProviderMessage<SimpleMessage>> allSimpleMessages = ListUtils.union(filteredSimpleMessages, summarizedMessages);
            return new ProcessedProviderMessageHolder(List.of(), allSimpleMessages);
        }
        return new ProcessedProviderMessageHolder(digestedMessages, filteredSimpleMessages);
    }

    private <T> List<T> filterProcessedMessages(List<T> processedProviderMessages) {
        return processedProviderMessages.stream().distinct().collect(Collectors.toList());
    }

}
