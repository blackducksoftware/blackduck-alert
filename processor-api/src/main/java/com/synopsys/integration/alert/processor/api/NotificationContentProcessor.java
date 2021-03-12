/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
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

    public ProviderMessageHolder processNotificationContent(ProcessingType processingType, List<NotificationContentWrapper> jobNotifications) {
        ProviderMessageHolder extractedProviderMessages = jobNotifications
                                                              .stream()
                                                              .map(providerMessageExtractionDelegator::extract)
                                                              .reduce(ProviderMessageHolder::reduce)
                                                              .orElse(ProviderMessageHolder.empty());

        return processExtractedNotifications(processingType, extractedProviderMessages);
    }

    private ProviderMessageHolder processExtractedNotifications(ProcessingType processingType, ProviderMessageHolder providerMessages) {
        if (ProcessingType.DEFAULT.equals(processingType)) {
            return providerMessages;
        }

        List<ProjectMessage> digestedMessages = projectMessageDigester.digest(providerMessages.getProjectMessages());
        if (ProcessingType.SUMMARY.equals(processingType)) {
            List<SimpleMessage> summarizedMessages = digestedMessages
                                                         .stream()
                                                         .map(projectMessageSummarizer::summarize)
                                                         .collect(Collectors.toList());
            List<SimpleMessage> allSimpleMessages = ListUtils.union(providerMessages.getSimpleMessages(), summarizedMessages);
            return new ProviderMessageHolder(List.of(), allSimpleMessages);
        }
        return new ProviderMessageHolder(digestedMessages, providerMessages.getSimpleMessages());
    }

}
