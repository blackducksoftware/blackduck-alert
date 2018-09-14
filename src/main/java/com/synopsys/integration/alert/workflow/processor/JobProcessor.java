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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.distribution.CommonDistributionConfigReader;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.model.TopicContent;
import com.synopsys.integration.alert.common.workflow.processor.TopicCollector;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.workflow.filter.FilterApplier;
import com.synopsys.integration.alert.workflow.filter.NotificationFilter;

@Component
public class JobProcessor {
    private final CommonDistributionConfigReader commonDistributionConfigReader;
    private final List<ProviderDescriptor> providerDescriptors;
    private final FilterApplier filterApplier;
    private final NotificationFilter notificationFilter;

    @Autowired
    public JobProcessor(final List<ProviderDescriptor> providerDescriptors, final CommonDistributionConfigReader commonDistributionConfigReader, final FilterApplier filterApplier, final NotificationFilter notificationFilter) {
        this.providerDescriptors = providerDescriptors;
        this.commonDistributionConfigReader = commonDistributionConfigReader;
        this.filterApplier = filterApplier;
        this.notificationFilter = notificationFilter;
    }

    public List<TopicContent> processNotifications(final FrequencyType frequency, final Collection<NotificationContent> notificationList) {

        final List<CommonDistributionConfig> unfilteredDistributionConfigs = commonDistributionConfigReader.getPopulatedConfigs();
        if (unfilteredDistributionConfigs.isEmpty()) {
            return Collections.emptyList();
        }

        final Predicate<CommonDistributionConfig> frequencyFilter = config -> frequency.name().equals(config.getFrequency());
        final List<CommonDistributionConfig> distributionConfigs = filterApplier.applyFilter(unfilteredDistributionConfigs, frequencyFilter);
        if (distributionConfigs.isEmpty()) {
            return Collections.emptyList();
        }

        final List<TopicContent> topicContentList = distributionConfigs.parallelStream().flatMap(jobConfiguration -> collectTopics(jobConfiguration, notificationList).stream()).collect(Collectors.toList());

        return topicContentList;
    }

    private Collection<NotificationContent> filterNotifications(final ProviderDescriptor providerDescriptor, final CommonDistributionConfig jobConfiguration, final Collection<NotificationContent> notificationCollection) {
        final Predicate<NotificationContent> providerFilter = (notificationContent) -> jobConfiguration.getProviderName().equals(notificationContent.getProvider());
        final Collection<NotificationContent> providerNotifications = filterApplier.applyFilter(notificationCollection, providerFilter);
        final Collection<NotificationContent> filteredNotificationList = notificationFilter.extractApplicableNotifications(providerDescriptor.getProviderContentTypes(), jobConfiguration, providerNotifications);
        return filteredNotificationList;
    }

    private List<TopicContent> collectTopics(final CommonDistributionConfig jobConfiguration, final Collection<NotificationContent> notificationCollection) {
        final Optional<ProviderDescriptor> providerDescriptor = providerDescriptors.parallelStream().filter(descriptor -> jobConfiguration.getProviderName().equals(descriptor.getName())).findFirst();

        if (!providerDescriptor.isPresent()) {
            return Collections.emptyList();
        } else {
            final Collection<NotificationContent> notificationsForJob = filterNotifications(providerDescriptor.get(), jobConfiguration, notificationCollection);
            if (notificationsForJob.isEmpty()) {
                return Collections.emptyList();
            }

            final FormatType formatType = FormatType.valueOf(jobConfiguration.getFormatType());
            final Map<String, TopicCollector> collectorMap = new HashMap<>();

            //TODO need better performance to map the notification type to the processor
            final Set<TopicCollector> providerTopicCollectors = providerDescriptor.get().createTopicCollectors();
            for (final TopicCollector collector : providerTopicCollectors) {
                for (final String notificationType : collector.getSupportedNotificationTypes()) {
                    collectorMap.put(notificationType, collector);
                }
            }

            notificationsForJob.parallelStream()
                .filter(notificationContent -> collectorMap.containsKey(notificationContent.getNotificationType()))
                .forEach(notificationContent -> collectorMap.get(notificationContent.getNotificationType()).insert(notificationContent));

            return collectorMap.values().parallelStream().flatMap(collector -> collector.collect(formatType).stream()).collect(Collectors.toList());
        }
    }
}
