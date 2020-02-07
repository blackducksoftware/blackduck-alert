/**
 * blackduck-alert
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
package com.synopsys.integration.alert.workflow.processor;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.ProviderContent;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.MessageContentCollector;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.workflow.filter.NotificationFilter;

@Component
public class MessageContentAggregator {
    private final ConfigurationAccessor jobConfigReader;
    private final List<Provider> providers;
    private final NotificationFilter notificationFilter;
    private final Map<FormatType, MessageContentProcessor> messageContentProcessorMap;

    @Autowired
    public MessageContentAggregator(final ConfigurationAccessor jobConfigReader, final List<Provider> providers, final NotificationFilter notificationFilter, final List<MessageContentProcessor> messageContentProcessorList) {
        this.jobConfigReader = jobConfigReader;
        this.providers = providers;
        this.notificationFilter = notificationFilter;
        this.messageContentProcessorMap = messageContentProcessorList.stream().collect(Collectors.toMap(MessageContentProcessor::getFormat, Function.identity()));
    }

    public Map<ConfigurationJobModel, List<MessageContentGroup>> processNotifications(final Collection<AlertNotificationWrapper> notificationList) {
        if (notificationList.isEmpty()) {
            return Map.of();
        }

        final List<ConfigurationJobModel> distributionConfigs = jobConfigReader.getAllJobs();
        if (distributionConfigs.isEmpty()) {
            return Map.of();
        }
        return processNotifications(distributionConfigs, notificationList);
    }

    public Map<ConfigurationJobModel, List<MessageContentGroup>> processNotifications(final FrequencyType frequency, final Collection<AlertNotificationWrapper> notificationList) {
        if (notificationList.isEmpty()) {
            return Map.of();
        }

        final List<ConfigurationJobModel> distributionConfigs = jobConfigReader.getAllJobs()
                                                                    .stream()
                                                                    .filter(commonDistributionConfiguration -> frequency.equals(commonDistributionConfiguration.getFrequencyType()))
                                                                    .collect(Collectors.toList());
        if (distributionConfigs.isEmpty()) {
            return Map.of();
        }

        return processNotifications(distributionConfigs, notificationList);
    }

    public Map<ConfigurationJobModel, List<MessageContentGroup>> processNotifications(final List<ConfigurationJobModel> distributionConfigs, final Collection<AlertNotificationWrapper> notificationList) {
        if (notificationList.isEmpty()) {
            return Map.of();
        }
        return distributionConfigs
                   .stream()
                   .collect(Collectors.toConcurrentMap(Function.identity(), jobConfig -> {
                       List<MessageContentGroup> groups = collectTopics(jobConfig, notificationList);
                       return groups;
                   }));
    }

    private List<MessageContentGroup> collectTopics(final ConfigurationJobModel jobConfiguration, final Collection<AlertNotificationWrapper> notificationCollection) {
        final Optional<Provider> optionalProvider = getProviderByName(jobConfiguration.getProviderName());
        if (optionalProvider.isPresent()) {
            final Provider provider = optionalProvider.get();
            final Collection<AlertNotificationWrapper> notificationsForJob = filterNotifications(provider, jobConfiguration, notificationCollection);
            if (notificationsForJob.isEmpty()) {
                return List.of();
            }

            final FormatType formatType = jobConfiguration.getFormatType();
            final Set<MessageContentCollector> providerMessageContentCollectors = provider.createTopicCollectors();
            final Map<String, MessageContentCollector> collectorMap = createCollectorMap(providerMessageContentCollectors);
            notificationsForJob.stream()
                .filter(notificationContent -> collectorMap.containsKey(notificationContent.getNotificationType()))
                .forEach(notificationContent -> collectorMap.get(notificationContent.getNotificationType()).insert(notificationContent));
            final List<ProviderMessageContent> messages = providerMessageContentCollectors
                                                              .stream()
                                                              .flatMap(collector -> collector.getCollectedContent().stream())
                                                              .sorted(Comparator.comparing(ProviderMessageContent::getProviderCreationTime))
                                                              .collect(Collectors.toList());
            return messageContentProcessorMap.get(formatType).process(messages);
        }
        return List.of();
    }

    private Optional<Provider> getProviderByName(final String name) {
        return providers.stream()
                   .filter(provider -> name.equals(provider.getName()))
                   .findFirst();
    }

    private Collection<AlertNotificationWrapper> filterNotifications(final Provider provider, final ConfigurationJobModel jobConfiguration, final Collection<AlertNotificationWrapper> notificationCollection) {
        final Predicate<AlertNotificationWrapper> providerFilter = notificationContent -> jobConfiguration.getProviderName().equals(notificationContent.getProvider());
        final Collection<AlertNotificationWrapper> providerNotifications = applyFilter(notificationCollection, providerFilter);

        final ProviderContent providerContent = provider.getProviderContent();
        return notificationFilter.extractApplicableNotifications(providerContent.getContentTypes(), jobConfiguration, providerNotifications);
    }

    private Map<String, MessageContentCollector> createCollectorMap(final Set<MessageContentCollector> providerMessageContentCollectors) {
        final Map<String, MessageContentCollector> collectorMap = new HashMap<>();

        //TODO need better performance to map the notification type to the processor
        for (final MessageContentCollector collector : providerMessageContentCollectors) {
            for (final String notificationType : collector.getSupportedNotificationTypes()) {
                collectorMap.put(notificationType, collector);
            }
        }
        return collectorMap;
    }

    private <T> List<T> applyFilter(final Collection<T> notificationList, final Predicate<T> filter) {
        return notificationList
                   .stream()
                   .filter(filter)
                   .collect(Collectors.toList());
    }

}
