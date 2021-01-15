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

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailer;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetails;
import com.synopsys.integration.alert.processor.api.digest.NotificationDigester;
import com.synopsys.integration.alert.processor.api.distribute.NotificationDistributor;
import com.synopsys.integration.alert.processor.api.extract.NotificationExtractor;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.filter.FilterableNotificationWrapper;
import com.synopsys.integration.alert.processor.api.filter.NotificationFilter;
import com.synopsys.integration.alert.processor.api.summarize.NotificationSummarizer;

public abstract class NotificationProcessor {
    private final NotificationPreProcessor preProcessor;
    private final NotificationFilter filter;
    private final NotificationExtractor extractor;
    private final NotificationDigester digester;
    private final NotificationDetailer detailer;
    private final NotificationSummarizer summarizer;
    private final NotificationDistributor distributor;

    protected NotificationProcessor(
        NotificationPreProcessor preProcessor,
        NotificationFilter filter,
        NotificationExtractor extractor,
        NotificationDigester digester,
        NotificationDetailer detailer,
        NotificationSummarizer summarizer,
        NotificationDistributor distributor
    ) {
        this.preProcessor = preProcessor;
        this.filter = filter;
        this.extractor = extractor;
        this.digester = digester;
        this.detailer = detailer;
        this.summarizer = summarizer;
        this.distributor = distributor;
    }

    public abstract void processNotifications(List<AlertNotificationModel> notifications);

    public abstract void processNotifications(FrequencyType frequency, List<AlertNotificationModel> notifications);

    protected abstract Map<DistributionJobModel, List<FilterableNotificationWrapper<?>>> mapJobsToNotificationsOrSomething(List<? extends FilterableNotificationWrapper<?>> filterableNotifications);

    // TODO include frequency?
    private void process(List<AlertNotificationModel> notifications) {
        List<? extends FilterableNotificationWrapper<?>> filterableNotifications = notifications
                                                                                       .stream()
                                                                                       .map(preProcessor::wrapNotification)
                                                                                       .collect(Collectors.toList());
        Map<DistributionJobModel, List<FilterableNotificationWrapper<?>>> jobsToNotifications = mapJobsToNotificationsOrSomething(filterableNotifications);
        for (Map.Entry<DistributionJobModel, List<FilterableNotificationWrapper<?>>> jobToNotifications : jobsToNotifications.entrySet()) {
            DistributionJobModel job = jobToNotifications.getKey();
            List<ProviderMessage<?>> extractedNotifications = filter.filter(job, jobToNotifications.getValue())
                                                                  .stream()
                                                                  .map(extractor::extract)
                                                                  .collect(Collectors.toList());

            NotificationDetails notificationDetails = processExtractedNotifications(job, extractedNotifications);
            distributor.distribute(job, notificationDetails);
        }
    }

    private NotificationDetails processExtractedNotifications(DistributionJobModel job, List<ProviderMessage<?>> extractedNotifications) {
        ProcessingType processingType = job.getProcessingType();
        if (ProcessingType.DEFAULT.equals(processingType)) {
            return detailer.detail(extractedNotifications);
        }

        List<ProviderMessage<?>> digestedNotifications = digester.digest(extractedNotifications);
        if (ProcessingType.SUMMARY.equals(processingType)) {
            List<SimpleMessage> summarizedNotifications = digestedNotifications
                                                              .stream()
                                                              .map(summarizer::summarize)
                                                              .collect(Collectors.toList());
            return new NotificationDetails(List.of(), summarizedNotifications);
        }
        return detailer.detail(digestedNotifications);
    }

}
