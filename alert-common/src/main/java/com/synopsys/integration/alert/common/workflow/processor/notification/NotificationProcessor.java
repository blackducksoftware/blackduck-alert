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
package com.synopsys.integration.alert.common.workflow.processor.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
import com.synopsys.integration.alert.common.workflow.processor.NotificationToDistributionEventConverter;
import com.synopsys.integration.alert.common.workflow.processor.ProviderMessageContentCollector;
import com.synopsys.integration.datastructure.SetMap;

@Component
public class NotificationProcessor {
    private final Logger logger = LoggerFactory.getLogger(NotificationProcessor.class);

    private final ConfigurationAccessor configurationAccessor;
    private final Map<String, Provider> providerKeyToProvider;
    private final NotificationToDistributionEventConverter notificationToEventConverter;

    @Autowired
    public NotificationProcessor(ConfigurationAccessor configurationAccessor, List<Provider> providers, NotificationToDistributionEventConverter notificationToEventConverter) {
        this.configurationAccessor = configurationAccessor;
        this.providerKeyToProvider = DataStructureUtils.mapToValues(providers, provider -> provider.getKey().getUniversalKey());
        this.notificationToEventConverter = notificationToEventConverter;
    }

    public List<DistributionEvent> processNotifications(FrequencyType frequency, List<AlertNotificationModel> notifications) {
        logger.info("Notifications to Process: {}", notifications.size());
        SetMap<NotificationFilterModel, AlertNotificationModel> notificationFilterMap = extractNotificationInformation(notifications);

        List<DistributionEvent> events = new ArrayList<>();
        for (Map.Entry<NotificationFilterModel, Set<AlertNotificationModel>> entry : notificationFilterMap.entrySet()) {
            NotificationFilterModel notificationFilterModel = entry.getKey();
            List<ConfigurationJobModel> matchingJobs = configurationAccessor.getMatchingEnabledJobs(frequency.name(), notificationFilterModel.getProviderConfigName(), notificationFilterModel.getNotificationType());

            List<AlertNotificationModel> matchingNotifications = new ArrayList<>(entry.getValue());
            if (!matchingNotifications.isEmpty() && !matchingJobs.isEmpty()) {
                events.addAll(processNotificationsThatMatchFilter(notificationFilterModel, matchingJobs, matchingNotifications));
            }
        }
        return events;
    }

    public List<DistributionEvent> processNotifications(List<AlertNotificationModel> notifications) {
        // used in AuditEntryActions
        // when a job is deleted use this method to send the same notification to the current set of jobs. i.e. audit
        SetMap<NotificationFilterModel, AlertNotificationModel> notificationFilterMap = extractNotificationInformation(notifications);

        List<DistributionEvent> events = new ArrayList<>();
        for (Map.Entry<NotificationFilterModel, Set<AlertNotificationModel>> entry : notificationFilterMap.entrySet()) {
            NotificationFilterModel notificationFilterModel = entry.getKey();
            List<ConfigurationJobModel> matchingJobs = configurationAccessor.getMatchingEnabledJobs(notificationFilterModel.getProviderConfigName(), notificationFilterModel.getNotificationType());

            List<AlertNotificationModel> matchingNotifications = new ArrayList<>(entry.getValue());
            if (!matchingNotifications.isEmpty() && !matchingJobs.isEmpty()) {
                events.addAll(processNotificationsThatMatchFilter(notificationFilterModel, matchingJobs, matchingNotifications));
            }
        }
        return events;
    }

    private List<DistributionEvent> processNotificationsThatMatchFilter(NotificationFilterModel notificationFilterModel, List<ConfigurationJobModel> matchingJobs, List<AlertNotificationModel> notifications) {
        Optional<ConfigurationModel> optionalProviderConfig = retrieveProviderConfig(notificationFilterModel.getProviderConfigName());
        if (optionalProviderConfig.isPresent()) {
            Provider provider = providerKeyToProvider.get(notificationFilterModel.getProvider());

            ConfigurationModel providerConfiguration = optionalProviderConfig.get();
            StatefulProvider statefulProvider = provider.createStatefulProvider(providerConfiguration);
            ProviderMessageContentCollector messageContentCollector = statefulProvider.getMessageContentCollector();

            ProviderDistributionFilter distributionFilter = statefulProvider.getDistributionFilter();

            return processNotificationsForJobs(messageContentCollector, distributionFilter, matchingJobs, notifications);
        } else {
            logger.warn("Could not find the provider config by the name {}. Skipping {} notifications.", notificationFilterModel.getProviderConfigName(), notifications.size());
        }
        return List.of();
    }

    public List<DistributionEvent> processNotifications(ConfigurationJobModel job, List<AlertNotificationModel> notifications) {
        // used in AuditEntryActions
        if (!job.isEnabled()) {
            logger.debug("Skipping disabled distribution job: {}", job.getName());
            return List.of();
        }
        if (notifications.isEmpty()) {
            return List.of();
        }

        Optional<ConfigurationModel> optionalProviderConfig = retrieveProviderConfig(job.getProviderConfigName());
        if (optionalProviderConfig.isPresent()) {
            Provider provider = providerKeyToProvider.get(job.getProviderName());

            ConfigurationModel providerConfiguration = optionalProviderConfig.get();
            StatefulProvider statefulProvider = provider.createStatefulProvider(providerConfiguration);

            ProviderDistributionFilter distributionFilter = statefulProvider.getDistributionFilter();
            List<AlertNotificationModel> notificationsByProviderConfig = filterNotificationsByProviderConfigId(statefulProvider, notifications);
            List<AlertNotificationModel> notificationsByType = filterNotificationsByType(job, notificationsByProviderConfig);
            List<AlertNotificationModel> filteredNotifications = filterNotificationsByProviderFields(job, distributionFilter, notificationsByType);

            logIgnoredNotifications(notifications, filteredNotifications);

            if (!filteredNotifications.isEmpty()) {
                logNotificationsThatMatchedJobs(filteredNotifications);

                ProviderMessageContentCollector messageContentCollector = statefulProvider.getMessageContentCollector();
                return createDistributionEventsForNotifications(messageContentCollector, job, distributionFilter.getCache(), filteredNotifications);
            }
        }
        return List.of();
    }

    private List<DistributionEvent> processNotifications(ProviderMessageContentCollector messageContentCollector, ProviderDistributionFilter distributionFilter, ConfigurationJobModel job, List<AlertNotificationModel> notifications) {
        List<AlertNotificationModel> filteredNotifications = filterNotificationsByProviderFields(job, distributionFilter, notifications);

        logIgnoredNotifications(notifications, filteredNotifications);

        if (!filteredNotifications.isEmpty()) {
            logNotificationsThatMatchedJobs(filteredNotifications);
            return createDistributionEventsForNotifications(messageContentCollector, job, distributionFilter.getCache(), filteredNotifications);
        }

        return List.of();
    }

    private SetMap<NotificationFilterModel, AlertNotificationModel> extractNotificationInformation(List<AlertNotificationModel> notifications) {
        SetMap<NotificationFilterModel, AlertNotificationModel> notificationFilterMap = SetMap.createDefault();
        for (AlertNotificationModel alertNotificationModel : notifications) {
            NotificationFilterModel notificationFilterModel = extractNotificationInformation(alertNotificationModel);
            notificationFilterMap.add(notificationFilterModel, alertNotificationModel);
        }
        return notificationFilterMap;
    }

    private NotificationFilterModel extractNotificationInformation(AlertNotificationModel alertNotificationModel) {
        String provider = alertNotificationModel.getProvider();
        String providerConfigName = alertNotificationModel.getProviderConfigName();
        String notificationType = alertNotificationModel.getNotificationType();

        return new NotificationFilterModel(provider, providerConfigName, notificationType);
    }

    private void logIgnoredNotifications(List<AlertNotificationModel> allNotifications, List<AlertNotificationModel> filteredNotifications) {
        if (logger.isDebugEnabled()) {
            List<AlertNotificationModel> ignoredNotifications = new ArrayList<>(allNotifications);
            ignoredNotifications.removeAll(filteredNotifications);
            if (!ignoredNotifications.isEmpty()) {
                logger.debug("Ignored {} notifications because they did not match any configured distribution job.", ignoredNotifications.size());
            }
        }
    }

    private void logNotificationsThatMatchedJobs(List<AlertNotificationModel> filteredNotifications) {
        if (logger.isDebugEnabled()) {
            List<Long> notificationIds = filteredNotifications.stream()
                                             .map(AlertNotificationModel::getId)
                                             .collect(Collectors.toList());
            logger.debug("These notificationIds matched distribution jobs: {}", notificationIds);
        }
    }

    private List<DistributionEvent> processNotificationsForJobs(ProviderMessageContentCollector messageContentCollector, ProviderDistributionFilter providerDistributionFilter, Collection<ConfigurationJobModel> jobs,
        List<AlertNotificationModel> notifications) {
        List<DistributionEvent> distributionEvents = new LinkedList<>();
        for (ConfigurationJobModel job : jobs) {
            List<DistributionEvent> distributionEventsForJob = processNotifications(messageContentCollector, providerDistributionFilter, job, notifications);
            distributionEvents.addAll(distributionEventsForJob);
        }
        return distributionEvents;
    }

    private List<AlertNotificationModel> filterNotificationsByType(ConfigurationJobModel job, List<AlertNotificationModel> notifications) {
        return notifications
                   .stream()
                   .filter(notification -> job.getNotificationTypes().contains(notification.getNotificationType()))
                   .collect(Collectors.toList());
    }

    private List<AlertNotificationModel> filterNotificationsByProviderConfigId(StatefulProvider provider, List<AlertNotificationModel> notifications) {
        return notifications
                   .stream()
                   .filter(notification -> notification.getProviderConfigId().equals(provider.getConfigId()))
                   .collect(Collectors.toList());
    }

    private List<AlertNotificationModel> filterNotificationsByProviderFields(ConfigurationJobModel job, ProviderDistributionFilter distributionFilter, List<AlertNotificationModel> notifications) {
        List<AlertNotificationModel> filteredNotifications = new LinkedList<>();
        for (AlertNotificationModel notification : notifications) {
            if (distributionFilter.doesNotificationApplyToConfiguration(notification, job)) {
                filteredNotifications.add(notification);
            }
        }
        return filteredNotifications;
    }

    private List<DistributionEvent> createDistributionEventsForNotifications(ProviderMessageContentCollector collector, ConfigurationJobModel job, NotificationDeserializationCache cache, List<AlertNotificationModel> notifications) {
        try {
            List<MessageContentGroup> messageGroups = collector.createMessageContentGroups(job, cache, notifications);
            return notificationToEventConverter.convertToEvents(job, messageGroups);
        } catch (AlertException e) {
            logger.error("Could not create distribution events", e);
        }
        return List.of();
    }

    private Optional<ConfigurationModel> retrieveProviderConfig(String providerConfigName) {
        try {
            return configurationAccessor.getProviderConfigurationByName(providerConfigName);
        } catch (AlertDatabaseConstraintException e) {
            logger.error("Could not retrieve the provider config for provider: {}", providerConfigName, e);
            return Optional.empty();
        }
    }

}
