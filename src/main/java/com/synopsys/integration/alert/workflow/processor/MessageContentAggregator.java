/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
import java.util.Collections;
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

import com.synopsys.integration.alert.common.configuration.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentCollector;
import com.synopsys.integration.alert.database.channel.JobConfigReader;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.workflow.filter.NotificationFilter;

@Component
public class MessageContentAggregator {
    private final JobConfigReader jobConfigReader;
    private final List<ProviderDescriptor> providerDescriptors;
    private final NotificationFilter notificationFilter;

    @Autowired
    public MessageContentAggregator(final JobConfigReader jobConfigReader, final List<ProviderDescriptor> providerDescriptors, final NotificationFilter notificationFilter) {
        this.jobConfigReader = jobConfigReader;
        this.providerDescriptors = providerDescriptors;
        this.notificationFilter = notificationFilter;
    }

    public Map<CommonDistributionConfiguration, List<AggregateMessageContent>> processNotifications(final Collection<NotificationContent> notificationList) {
        if (notificationList.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<CommonDistributionConfiguration> distributionConfigs = jobConfigReader.getPopulatedConfigs();
        if (distributionConfigs.isEmpty()) {
            return Collections.emptyMap();
        }
        return processNotifications(distributionConfigs, notificationList);
    }

    public Map<CommonDistributionConfiguration, List<AggregateMessageContent>> processNotifications(final FrequencyType frequency, final Collection<NotificationContent> notificationList) {
        if (notificationList.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<CommonDistributionConfiguration> unfilteredDistributionConfigs = jobConfigReader.getPopulatedConfigs();
        if (unfilteredDistributionConfigs.isEmpty()) {
            return Collections.emptyMap();
        }
        final List<CommonDistributionConfiguration> distributionConfigs = unfilteredDistributionConfigs
                                                                                  .stream()
                                                                                  .filter(config -> frequency.equals(config.getFrequencyType()))
                                                                                  .collect(Collectors.toList());
        if (distributionConfigs.isEmpty()) {
            return Collections.emptyMap();
        }

        return processNotifications(distributionConfigs, notificationList);
    }

    public Map<CommonDistributionConfiguration, List<AggregateMessageContent>> processNotifications(final List<CommonDistributionConfiguration> distributionConfigs, final Collection<NotificationContent> notificationList) {
        if (notificationList.isEmpty()) {
            return Collections.emptyMap();
        }
        return distributionConfigs
                       .stream()
                       .collect(Collectors.toConcurrentMap(Function.identity(), jobConfig -> collectTopics(jobConfig, notificationList)));
    }

    private List<AggregateMessageContent> collectTopics(final CommonDistributionConfiguration jobConfiguration, final Collection<NotificationContent> notificationCollection) {
        final Optional<ProviderDescriptor> providerDescriptor = getProviderDescriptorByName(jobConfiguration.getProviderName());
        if (providerDescriptor.isPresent()) {
            final Collection<NotificationContent> notificationsForJob = filterNotifications(providerDescriptor.get(), jobConfiguration, notificationCollection);
            if (notificationsForJob.isEmpty()) {
                return Collections.emptyList();
            }

            final FormatType formatType = jobConfiguration.getFormatType();
            final Set<MessageContentCollector> providerMessageContentCollectors = providerDescriptor.get().createTopicCollectors();
            final Map<String, MessageContentCollector> collectorMap = createCollectorMap(providerMessageContentCollectors);
            notificationsForJob.stream()
                    .filter(notificationContent -> collectorMap.containsKey(notificationContent.getNotificationType()))
                    .forEach(notificationContent -> collectorMap.get(notificationContent.getNotificationType()).insert(notificationContent));
            final List<AggregateMessageContent> collectedTopics = providerMessageContentCollectors
                                                                          .stream()
                                                                          .flatMap(collector -> collector.collect(formatType).stream())
                                                                          .collect(Collectors.toList());
            return collectedTopics;
        }
        return Collections.emptyList();
    }

    private Optional<ProviderDescriptor> getProviderDescriptorByName(final String name) {
        return providerDescriptors.stream()
                       .filter(descriptor -> name.equals(descriptor.getName()))
                       .findFirst();
    }

    private Collection<NotificationContent> filterNotifications(final ProviderDescriptor providerDescriptor, final CommonDistributionConfiguration jobConfiguration, final Collection<NotificationContent> notificationCollection) {
        final Predicate<NotificationContent> providerFilter = (notificationContent) -> jobConfiguration.getProviderName().equals(notificationContent.getProvider());
        final Collection<NotificationContent> providerNotifications = applyFilter(notificationCollection, providerFilter);
        final Collection<NotificationContent> filteredNotificationList = notificationFilter.extractApplicableNotifications(providerDescriptor.getProviderContentTypes(), jobConfiguration, providerNotifications);
        return filteredNotificationList;
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
