/**
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
package com.synopsys.integration.alert.processor.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.ProviderMessageDetailer;
import com.synopsys.integration.alert.processor.api.detail.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.digest.ProjectMessageDigester;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.extract.ProviderMessageExtractor;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.filter.FilterableNotificationExtractor;
import com.synopsys.integration.alert.processor.api.filter.JobNotificationExtractor;
import com.synopsys.integration.alert.processor.api.filter.JobNotificationFilter;
import com.synopsys.integration.alert.processor.api.filter.model.FilterableNotificationWrapper;
import com.synopsys.integration.alert.processor.api.summarize.ProjectMessageSummarizer;

@Component
public final class NotificationProcessorV2 {
    private final FilterableNotificationExtractor filterableNotificationExtractor;
    private final JobNotificationFilter jobNotificationFilter;
    private final JobNotificationExtractor jobNotificationExtractor;
    private final ProviderMessageExtractor providerMessageExtractor;
    private final ProjectMessageDigester projectMessageDigester;
    private final ProviderMessageDetailer providerMessageDetailer;
    private final ProjectMessageSummarizer projectMessageSummarizer;
    private final ProviderMessageDistributor providerMessageDistributor;

    @Autowired
    protected NotificationProcessorV2(
        FilterableNotificationExtractor filterableNotificationExtractor,
        JobNotificationFilter jobNotificationFilter,
        JobNotificationExtractor jobNotificationExtractor,
        ProviderMessageExtractor providerMessageExtractor,
        ProjectMessageDigester projectMessageDigester,
        ProviderMessageDetailer providerMessageDetailer,
        ProjectMessageSummarizer projectMessageSummarizer,
        ProviderMessageDistributor providerMessageDistributor
    ) {
        this.filterableNotificationExtractor = filterableNotificationExtractor;
        this.jobNotificationFilter = jobNotificationFilter;
        this.jobNotificationExtractor = jobNotificationExtractor;
        this.providerMessageExtractor = providerMessageExtractor;
        this.projectMessageDigester = projectMessageDigester;
        this.providerMessageDetailer = providerMessageDetailer;
        this.projectMessageSummarizer = projectMessageSummarizer;
        this.providerMessageDistributor = providerMessageDistributor;
    }

    public final void processNotifications(List<AlertNotificationModel> notifications) {
        processNotifications(notifications, null);
    }

    public final void processNotifications(List<AlertNotificationModel> notifications, @Nullable FrequencyType frequency) {
        List<? extends FilterableNotificationWrapper<?>> filterableNotifications = notifications
                                                                                       .stream()
                                                                                       .map(filterableNotificationExtractor::wrapNotification)
                                                                                       .collect(Collectors.toList());
        Map<DistributionJobModel, List<FilterableNotificationWrapper<?>>> jobsToNotifications = jobNotificationExtractor.mapJobsToNotifications(filterableNotifications, frequency);
        for (Map.Entry<DistributionJobModel, List<FilterableNotificationWrapper<?>>> jobToNotifications : jobsToNotifications.entrySet()) {
            processJobNotifications(jobToNotifications.getKey(), jobToNotifications.getValue());
        }
    }

    private void processJobNotifications(DistributionJobModel job, List<FilterableNotificationWrapper<?>> jobNotifications) {
        ProviderMessageHolder extractedProviderMessages = jobNotificationFilter.filter(job, jobNotifications)
                                                              .stream()
                                                              .map(providerMessageExtractor::extract)
                                                              .reduce(ProviderMessageHolder::reduce)
                                                              .orElse(ProviderMessageHolder.empty());

        ProviderMessageHolder processedProviderMessages = processExtractedNotifications(job, extractedProviderMessages);
        providerMessageDistributor.distribute(job, processedProviderMessages);
    }

    private ProviderMessageHolder processExtractedNotifications(DistributionJobModel job, ProviderMessageHolder providerMessages) {
        ProcessingType processingType = job.getProcessingType();
        if (ProcessingType.DEFAULT.equals(processingType)) {
            return providerMessageDetailer.detail(providerMessages);
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
        return providerMessageDetailer.detail(providerMessages);
    }

}
