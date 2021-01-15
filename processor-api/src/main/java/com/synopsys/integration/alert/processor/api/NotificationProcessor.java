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

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.MessageDetailer;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetails;
import com.synopsys.integration.alert.processor.api.digest.MessageDigester;
import com.synopsys.integration.alert.processor.api.distribute.MessageDistributor;
import com.synopsys.integration.alert.processor.api.extract.MessageExtractor;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.filter.FilterableNotificationWrapper;
import com.synopsys.integration.alert.processor.api.filter.NotificationFilter;
import com.synopsys.integration.alert.processor.api.summarize.MessageSummarizer;

public abstract class NotificationProcessor {
    private final FilterableNotificationExtractor filterableNotificationExtractor;
    private final NotificationFilter filter;
    private final MessageExtractor messageExtractor;
    private final MessageDigester messageDigester;
    private final MessageDetailer messageDetailer;
    private final MessageSummarizer messageSummarizer;
    private final MessageDistributor messageDistributor;

    protected NotificationProcessor(
        FilterableNotificationExtractor filterableNotificationExtractor,
        NotificationFilter filter,
        MessageExtractor messageExtractor,
        MessageDigester messageDigester,
        MessageDetailer messageDetailer,
        MessageSummarizer messageSummarizer,
        MessageDistributor messageDistributor
    ) {
        this.filterableNotificationExtractor = filterableNotificationExtractor;
        this.filter = filter;
        this.messageExtractor = messageExtractor;
        this.messageDigester = messageDigester;
        this.messageDetailer = messageDetailer;
        this.messageSummarizer = messageSummarizer;
        this.messageDistributor = messageDistributor;
    }

    public final void processNotifications(List<AlertNotificationModel> notifications) {
        processNotifications(notifications, null);
    }

    public final void processNotifications(List<AlertNotificationModel> notifications, @Nullable FrequencyType frequency) {
        List<? extends FilterableNotificationWrapper<?>> filterableNotifications = notifications
                                                                                       .stream()
                                                                                       .map(filterableNotificationExtractor::wrapNotification)
                                                                                       .collect(Collectors.toList());
        Map<DistributionJobModel, List<FilterableNotificationWrapper<?>>> jobsToNotifications = mapJobsToNotifications(filterableNotifications, frequency);
        for (Map.Entry<DistributionJobModel, List<FilterableNotificationWrapper<?>>> jobToNotifications : jobsToNotifications.entrySet()) {
            DistributionJobModel job = jobToNotifications.getKey();
            List<ProviderMessage<?>> extractedNotifications = filter.filter(job, jobToNotifications.getValue())
                                                                  .stream()
                                                                  .map(messageExtractor::extract)
                                                                  .collect(Collectors.toList());

            NotificationDetails notificationDetails = processExtractedNotifications(job, extractedNotifications);
            messageDistributor.distribute(job, notificationDetails);
        }
    }

    protected abstract Map<DistributionJobModel, List<FilterableNotificationWrapper<?>>> mapJobsToNotifications(List<? extends FilterableNotificationWrapper<?>> filterableNotifications, @Nullable FrequencyType frequency);

    private NotificationDetails processExtractedNotifications(DistributionJobModel job, List<ProviderMessage<?>> extractedNotifications) {
        ProcessingType processingType = job.getProcessingType();
        if (ProcessingType.DEFAULT.equals(processingType)) {
            return messageDetailer.detail(extractedNotifications);
        }

        List<ProviderMessage<?>> digestedNotifications = messageDigester.digest(extractedNotifications);
        if (ProcessingType.SUMMARY.equals(processingType)) {
            List<SimpleMessage> summarizedNotifications = digestedNotifications
                                                              .stream()
                                                              .map(messageSummarizer::summarize)
                                                              .collect(Collectors.toList());
            return new NotificationDetails(List.of(), summarizedNotifications);
        }
        return messageDetailer.detail(digestedNotifications);
    }

}
