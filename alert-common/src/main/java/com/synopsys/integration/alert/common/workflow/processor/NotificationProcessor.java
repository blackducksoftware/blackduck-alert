/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.workflow.processor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;

@Component
public class NotificationProcessor {
    private final Logger logger = LoggerFactory.getLogger(NotificationProcessor.class);

    private ConfigurationAccessor configurationAccessor;
    private Map<String, Provider> providerKeyToProvider;
    private NotificationToDistributionEventConverter notificationToEventConverter;

    @Autowired
    public NotificationProcessor(ConfigurationAccessor configurationAccessor, List<Provider> providers, NotificationToDistributionEventConverter notificationToEventConverter) {
        this.configurationAccessor = configurationAccessor;
        this.providerKeyToProvider = DataStructureUtils.mapToValues(providers, provider -> provider.getKey().getUniversalKey());
        this.notificationToEventConverter = notificationToEventConverter;
    }

    public List<DistributionEvent> processNotifications(FrequencyType frequency, List<AlertNotificationWrapper> notifications) {
        logger.info("Notifications to Process: {}", notifications.size());
        List<ConfigurationJobModel> jobsForFrequency = configurationAccessor.getJobsByFrequency(frequency);
        return processNotificationsForJobs(jobsForFrequency, notifications);
    }

    public List<DistributionEvent> processNotifications(List<AlertNotificationWrapper> notifications) {
        // when a job is deleted use this method to send the same notification to the current set of jobs. i.e. audit
        List<ConfigurationJobModel> allJobs = configurationAccessor.getAllJobs();
        return processNotificationsForJobs(allJobs, notifications);
    }

    public List<DistributionEvent> processNotifications(ConfigurationJobModel job, List<AlertNotificationWrapper> notifications) {
        if (notifications.isEmpty()) {
            return List.of();
        }

        Provider provider = providerKeyToProvider.get(job.getProviderName());
        ProviderDistributionFilter distributionFilter = provider.createDistributionFilter();
        List<AlertNotificationWrapper> notificationsByType = filterNotificationsByType(job, notifications);
        List<AlertNotificationWrapper> filteredNotifications = filterNotificationsByProviderFields(job, distributionFilter, notificationsByType);

        if (!filteredNotifications.isEmpty()) {
            ProviderMessageContentCollector messageContentCollector = provider.createMessageContentCollector();
            return createDistributionEventsForNotifications(messageContentCollector, job, distributionFilter.getCache(), filteredNotifications);
        }
        return List.of();
    }

    private List<DistributionEvent> processNotificationsForJobs(Collection<ConfigurationJobModel> jobs, List<AlertNotificationWrapper> notifications) {
        List<DistributionEvent> distributionEvents = new LinkedList<>();
        for (ConfigurationJobModel job : jobs) {
            List<DistributionEvent> distributionEventsForJob = processNotifications(job, notifications);
            distributionEvents.addAll(distributionEventsForJob);
        }
        return distributionEvents;
    }

    private List<AlertNotificationWrapper> filterNotificationsByType(ConfigurationJobModel job, List<AlertNotificationWrapper> notifications) {
        return notifications
                   .stream()
                   .filter(notification -> job.getNotificationTypes().contains(notification.getNotificationType()))
                   .collect(Collectors.toList());
    }

    private List<AlertNotificationWrapper> filterNotificationsByProviderFields(ConfigurationJobModel job, ProviderDistributionFilter distributionFilter, List<AlertNotificationWrapper> notifications) {
        List<AlertNotificationWrapper> filteredNotifications = new LinkedList<>();
        for (AlertNotificationWrapper notification : notifications) {
            if (distributionFilter.doesNotificationApplyToConfiguration(notification, job)) {
                filteredNotifications.add(notification);
            }
        }
        return filteredNotifications;
    }

    private List<DistributionEvent> createDistributionEventsForNotifications(ProviderMessageContentCollector collector, ConfigurationJobModel job, NotificationDeserializationCache cache, List<AlertNotificationWrapper> notifications) {
        try {
            List<MessageContentGroup> messageGroups = collector.createMessageContentGroups(job, cache, notifications);
            return notificationToEventConverter.convertToEvents(job, messageGroups);
        } catch (AlertException e) {
            logger.error("Could not create distribution events", e);
        }
        return List.of();
    }

}
