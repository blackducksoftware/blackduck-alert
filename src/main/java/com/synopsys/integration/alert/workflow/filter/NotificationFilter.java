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

import java.util.ArrayList;
import java.util.Arrays;
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.distribution.CommonDistributionConfigReader;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.workflow.filter.builder.AndFieldFilterBuilder;
import com.synopsys.integration.alert.workflow.filter.builder.DefaultFilterBuilders;
import com.synopsys.integration.alert.workflow.filter.builder.JsonFieldFilterBuilder;
import com.synopsys.integration.alert.workflow.filter.builder.JsonFilterBuilder;
import com.synopsys.integration.alert.workflow.filter.builder.OrFieldFilterBuilder;

@Component
public class NotificationFilter {
    final CommonDistributionConfigReader commonDistributionConfigReader;
    private final Gson gson;
    private final List<ProviderDescriptor> providerDescriptors;

    @Autowired
    public NotificationFilter(final Gson gson, final List<ProviderDescriptor> providerDescriptors, final CommonDistributionConfigReader commonDistributionConfigReader) {
        this.gson = gson;
        this.providerDescriptors = providerDescriptors;
        this.commonDistributionConfigReader = commonDistributionConfigReader;
    }

    /**
     * Creates a java.util.Collection of NotificationContent objects that are applicable for at least one Distribution Job.
     * @return A java.util.List of sorted (by createdAt) NotificationContent objects.
     */
    public Collection<NotificationContent> extractApplicableNotifications(final FrequencyType frequency, final Collection<NotificationContent> notificationList) {
        final List<CommonDistributionConfig> unfilteredDistributionConfigs = commonDistributionConfigReader.getPopulatedConfigs();
        if (unfilteredDistributionConfigs.isEmpty()) {
            return Collections.emptyList();
        }

        final Predicate<CommonDistributionConfig> frequencyFilter = config -> frequency.name().equals(config.getFrequency());
        final List<CommonDistributionConfig> distributionConfigs = applyFilter(unfilteredDistributionConfigs, frequencyFilter);
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
            final Set<HierarchicalField> filterableFields = getFilterableFieldsByNotificationType(type, providerContentTypes);
            final Predicate<NotificationContent> filterForNotificationType = createFilter(filterableFields, distributionConfigs);

            final List<NotificationContent> matchingNotifications = applyFilter(groupedNotifications, filterForNotificationType);
            filteredNotifications.addAll(matchingNotifications);
        });

        return filteredNotifications
               .parallelStream()
               .sorted(Comparator.comparing(NotificationContent::getCreatedAt))
               .collect(Collectors.toList());
    }

    private Set<String> getConfiguredNotificationTypes(final List<CommonDistributionConfig> distributionConfigs) {
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

    public List<ProviderContentType> getProviderContentTypes() {
        return providerDescriptors
               .parallelStream()
               .flatMap(descriptor -> descriptor.getProviderContentTypes().stream())
               .collect(Collectors.toList());
    }

    public Set<HierarchicalField> getFilterableFieldsByNotificationType(final String notificationType, final List<ProviderContentType> contentTypes) {
        return contentTypes
               .parallelStream()
               .filter(contentType -> notificationType.equals(contentType.getNotificationType()))
               .flatMap(contentType -> contentType.getFilterableFields().stream())
               .collect(Collectors.toSet());
    }

    private Predicate<NotificationContent> createFilter(final Collection<HierarchicalField> filterableFields, final Collection<CommonDistributionConfig> distributionConfigs) {
        JsonFilterBuilder filterBuilder = DefaultFilterBuilders.ALWAYS_TRUE;
        for (final CommonDistributionConfig config : distributionConfigs) {
            for (final HierarchicalField field : filterableFields) {
                final Collection<String> valuesFromField = getValuesFromConfig(field.getConfigNameMapping(), config);
                if (valuesFromField != null) {
                    final JsonFilterBuilder fieldFilter = createFilterBuilderForAllValues(field, valuesFromField);
                    filterBuilder = new AndFieldFilterBuilder(filterBuilder, fieldFilter);
                }
            }
        }
        return filterBuilder.buildPredicate();
    }

    private Collection<String> getValuesFromConfig(final String configKey, final CommonDistributionConfig config) {
        if (!shouldFilter(config)) {
            // FIXME this is extremely specific and we need a way to avoid it
            return null;
        }
        final JsonObject jsonConfig = gson.toJsonTree(config).getAsJsonObject();
        final JsonElement jsonElement = jsonConfig.get(configKey);

        List<String> values = null;
        if (jsonElement.isJsonPrimitive()) {
            return Arrays.asList(jsonElement.getAsString());
        } else if (jsonElement.isJsonArray()) {
            values = new ArrayList<>();
            for (final JsonElement arrayElement : jsonElement.getAsJsonArray()) {
                values.add(arrayElement.getAsString());
            }
        }
        return values;
    }

    private JsonFilterBuilder createFilterBuilderForAllValues(final HierarchicalField field, final Collection<String> applicableValues) {
        JsonFilterBuilder filterBuilderForAllValues = DefaultFilterBuilders.ALWAYS_FALSE;

        for (final String value : applicableValues) {
            final JsonFilterBuilder filterBuilderForValue = new JsonFieldFilterBuilder(gson, field, value);
            filterBuilderForAllValues = new OrFieldFilterBuilder(filterBuilderForAllValues, filterBuilderForValue);
        }
        return filterBuilderForAllValues;
    }

    private <T> List<T> applyFilter(final Collection<T> notificationList, final Predicate<T> filter) {
        return notificationList
               .parallelStream()
               .filter(filter)
               .collect(Collectors.toList());
    }

    // TODO find a way to get rid of this
    private boolean shouldFilter(final CommonDistributionConfig config) {
        final String filterByProject = config.getFilterByProject();
        return Boolean.parseBoolean(filterByProject);
    }
}
