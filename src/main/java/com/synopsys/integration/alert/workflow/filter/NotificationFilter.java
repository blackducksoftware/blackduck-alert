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
package com.synopsys.integration.alert.workflow.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.field.JsonField;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.database.channel.JobConfigReader;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.workflow.filter.builder.AndFieldFilterBuilder;
import com.synopsys.integration.alert.workflow.filter.builder.DefaultFilterBuilders;
import com.synopsys.integration.alert.workflow.filter.builder.JsonFieldFilterBuilder;
import com.synopsys.integration.alert.workflow.filter.builder.JsonFilterBuilder;
import com.synopsys.integration.alert.workflow.filter.builder.OrFieldFilterBuilder;
import com.synopsys.integration.alert.workflow.filter.field.JsonExtractor;

@Component
public class NotificationFilter {
    private final JobConfigReader jobConfigReader;
    private final JsonExtractor jsonExtractor;
    private final List<ProviderDescriptor> providerDescriptors;

    @Autowired
    public NotificationFilter(final JsonExtractor jsonExtractor, final List<ProviderDescriptor> providerDescriptors, final JobConfigReader jobConfigReader) {
        this.jsonExtractor = jsonExtractor;
        this.providerDescriptors = providerDescriptors;
        this.jobConfigReader = jobConfigReader;
    }

    /**
     * Creates a java.util.Collection of NotificationContent objects that are applicable for at least one Distribution Job.
     * @return A java.util.List of sorted (by createdAt) NotificationContent objects.
     */
    public Collection<NotificationContent> extractApplicableNotifications(final FrequencyType frequency, final Collection<NotificationContent> notificationList) {
        final List<? extends CommonDistributionConfig> unfilteredDistributionConfigs = jobConfigReader.getPopulatedConfigs();
        if (unfilteredDistributionConfigs.isEmpty()) {
            return Collections.emptyList();
        }

        final List<? extends CommonDistributionConfig> distributionConfigs = unfilteredDistributionConfigs
                                                                                 .parallelStream()
                                                                                 .filter(config -> frequency.name().equals(config.getFrequency()))
                                                                                 .collect(Collectors.toList());

        if (distributionConfigs.isEmpty()) {
            return Collections.emptyList();
        }

        final Set<String> configuredNotificationTypes = getConfiguredNotificationTypes(distributionConfigs);
        if (configuredNotificationTypes.isEmpty()) {
            return Collections.emptyList();
        }

        final List<ProviderContentType> providerContentTypes = getProviderContentTypes();
        final Map<String, List<NotificationContent>> notificationsByType = getNotificationsByType(configuredNotificationTypes, notificationList);

        final Set<NotificationContent> filteredNotifications = new HashSet<>();
        notificationsByType.forEach((type, groupedNotifications) -> {
            final Set<JsonField> filterableFields = getFilterableFieldsByNotificationType(type, providerContentTypes);
            final Predicate<NotificationContent> filterForNotificationType = createFilter(filterableFields, distributionConfigs);

            final List<NotificationContent> matchingNotifications = applyFilter(groupedNotifications, filterForNotificationType);
            filteredNotifications.addAll(matchingNotifications);
        });
        return filteredNotifications
                   .parallelStream()
                   .sorted(Comparator.comparing(NotificationContent::getCreatedAt))
                   .collect(Collectors.toList());
    }

    /**
     * Creates a java.util.Collection of NotificationContent objects that are applicable for at least one Distribution Job.
     * @return A java.util.List of sorted (by createdAt) NotificationContent objects.
     */
    public Collection<NotificationContent> extractApplicableNotifications(final Set<ProviderContentType> providerContentTypes, final CommonDistributionConfig jobConfiguration, final Collection<NotificationContent> notificationList) {
        final Set<String> configuredNotificationTypes = new HashSet<>(jobConfiguration.getNotificationTypes());
        if (configuredNotificationTypes.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<String, List<NotificationContent>> notificationsByType = getNotificationsByType(configuredNotificationTypes, notificationList);

        final Set<NotificationContent> filteredNotifications = new HashSet<>();
        notificationsByType.forEach((type, groupedNotifications) -> {
            final Set<JsonField> filterableFields = getFilterableFieldsByNotificationType(type, providerContentTypes);
            final Predicate<NotificationContent> filterForNotificationType = createJobFilter(filterableFields, jobConfiguration);

            final List<NotificationContent> matchingNotifications = applyFilter(groupedNotifications, filterForNotificationType);
            filteredNotifications.addAll(matchingNotifications);
        });
        return filteredNotifications
                   .parallelStream()
                   .sorted(Comparator.comparing(NotificationContent::getProviderCreationTime))
                   .collect(Collectors.toList());
    }

    private Set<String> getConfiguredNotificationTypes(final List<? extends CommonDistributionConfig> distributionConfigs) {
        return distributionConfigs
                   .parallelStream()
                   .flatMap(config -> config.getNotificationTypes().stream())
                   .collect(Collectors.toSet());
    }

    private Map<String, List<NotificationContent>> getNotificationsByType(final Set<String> notificationTypes, final Collection<NotificationContent> notifications) {
        final Map<String, List<NotificationContent>> notificationsByType = new HashMap<>();
        notificationTypes.parallelStream().forEach(type -> {
            final List<NotificationContent> applicableNotifications = notifications
                                                                          .stream()
                                                                          .filter(notification -> type.equals(notification.getNotificationType()))
                                                                          .collect(Collectors.toList());
            notificationsByType.put(type, applicableNotifications);
        });
        return notificationsByType;
    }

    // TODO since this is used with the MessageContentAggregator we don't need to iterate again; this can be removed
    private List<ProviderContentType> getProviderContentTypes() {
        return providerDescriptors
                   .parallelStream()
                   .flatMap(descriptor -> descriptor.getProviderContentTypes().stream())
                   .collect(Collectors.toList());
    }

    private Set<JsonField> getFilterableFieldsByNotificationType(final String notificationType, final Collection<ProviderContentType> contentTypes) {
        return contentTypes
                   .parallelStream()
                   .filter(contentType -> notificationType.equals(contentType.getNotificationType()))
                   .flatMap(contentType -> contentType.getFilterableFields().stream())
                   .collect(Collectors.toSet());
    }

    private Predicate<NotificationContent> createFilter(final Collection<JsonField> filterableFields, final Collection<? extends CommonDistributionConfig> distributionConfigs) {
        Predicate<NotificationContent> orPredicate = DefaultFilterBuilders.ALWAYS_FALSE.buildPredicate();
        for (final CommonDistributionConfig config : distributionConfigs) {
            orPredicate = orPredicate.or(createJobFilter(filterableFields, config));
        }
        return orPredicate;
    }

    private Predicate<NotificationContent> createJobFilter(final Collection<JsonField> filterableFields, final CommonDistributionConfig config) {
        JsonFilterBuilder filterBuilder = DefaultFilterBuilders.ALWAYS_TRUE;
        if (shouldFilter(config)) {
            for (final JsonField field : filterableFields) {
                final Collection<String> valuesFromField = jsonExtractor.getValuesFromConfig(field, config);
                final JsonFilterBuilder fieldFilter = createFilterBuilderForAllValues(field, valuesFromField);
                filterBuilder = new AndFieldFilterBuilder(filterBuilder, fieldFilter);
            }
        }
        return filterBuilder.buildPredicate();
    }

    private JsonFilterBuilder createFilterBuilderForAllValues(final JsonField field, final Collection<String> applicableValues) {
        JsonFilterBuilder filterBuilderForAllValues = DefaultFilterBuilders.ALWAYS_FALSE;
        for (final String value : applicableValues) {
            final JsonFilterBuilder filterBuilderForValue = new JsonFieldFilterBuilder(jsonExtractor, field, value);
            filterBuilderForAllValues = new OrFieldFilterBuilder(filterBuilderForAllValues, filterBuilderForValue);
        }
        return filterBuilderForAllValues;
    }

    // FIXME this is extremely specific and we need a way to avoid it
    private boolean shouldFilter(final CommonDistributionConfig config) {
        final String filterByProject = config.getFilterByProject();
        return Boolean.parseBoolean(filterByProject);
    }

    private <T> List<T> applyFilter(final Collection<T> notificationList, final Predicate<T> filter) {
        return notificationList
                   .parallelStream()
                   .filter(filter)
                   .collect(Collectors.toList());
    }
}
