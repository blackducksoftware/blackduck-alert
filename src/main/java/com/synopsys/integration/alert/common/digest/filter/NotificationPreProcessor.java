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
package com.synopsys.integration.alert.common.digest.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.synopsys.integration.alert.common.descriptor.config.RestApi;
import com.synopsys.integration.alert.common.enumeration.DigestType;
import com.synopsys.integration.alert.common.enumeration.RestApiType;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.workflow.filter.AndFieldFilterBuilder;
import com.synopsys.integration.alert.workflow.filter.DefaultFilterBuilders;
import com.synopsys.integration.alert.workflow.filter.JsonFieldFilter;
import com.synopsys.integration.alert.workflow.filter.JsonFilterBuilder;
import com.synopsys.integration.alert.workflow.filter.OrFieldFilterBuilder;

@Component
public class NotificationPreProcessor {
    private final Gson gson;
    private final List<ProviderDescriptor> providerDescriptors;
    private final DigestType frequency;

    @Autowired
    public NotificationPreProcessor(final Gson gson, final List<ProviderDescriptor> providerDescriptors, final DigestType frequency) {
        this.gson = gson;
        this.providerDescriptors = providerDescriptors;
        this.frequency = frequency;
    }

    public Collection<NotificationContent> process(final Collection<NotificationContent> notificationList) {
        final Set<NotificationContent> filteredNotifications = new HashSet<>();

        final List<CommonDistributionConfig> unfilteredDistributionConfigs = getCommonDistributionConfigs();
        if (unfilteredDistributionConfigs.isEmpty()) {
            return filteredNotifications;
        }

        final Predicate<CommonDistributionConfig> frequencyFilter = config -> frequency.equals(config.getFrequency());
        final List<CommonDistributionConfig> distributionConfigs = applyFilter(unfilteredDistributionConfigs, frequencyFilter);
        if (distributionConfigs.isEmpty()) {
            return filteredNotifications;
        }

        final Set<String> configuredNotificationTypes = getConfiguredNotificationTypes(distributionConfigs);
        if (configuredNotificationTypes.isEmpty()) {
            return filteredNotifications;
        }

        final List<ProviderContentType> providerContentTypes = getProviderContentTypes();
        final Map<String, List<NotificationContent>> notificationsByType = getNotificationsByType(configuredNotificationTypes, notificationList);

        notificationsByType.forEach((type, groupedNotifications) -> {
            final Set<HierarchicalField> filterableFields = getFilterableFieldsByNotificationType(type, providerContentTypes);
            Predicate<NotificationContent> filterForNotificationType = createFilter(filterableFields, distributionConfigs);

            final List<NotificationContent> matchingNotifications = applyFilter(groupedNotifications, filterForNotificationType);
            filteredNotifications.addAll(matchingNotifications);
        });

        return filteredNotifications
                   .parallelStream()
                   .sorted(Comparator.comparing(NotificationContent::getCreatedAt))
                   .collect(Collectors.toList());
    }

    private List<CommonDistributionConfig> getCommonDistributionConfigs() {
        final List<CommonDistributionConfig> commonDistributionConfigs = new ArrayList<>();
        providerDescriptors.forEach(descriptor -> {
            final RestApi restApi = descriptor.getRestApi(RestApiType.CHANNEL_DISTRIBUTION_CONFIG);
            final List<? extends DatabaseEntity> entities = restApi.readEntities();

            entities.forEach(entity -> {
                if (entity.getClass().isInstance(CommonDistributionConfigEntity.class)) {
                    final CommonDistributionConfig config = (CommonDistributionConfig) restApi.populateConfigFromEntity(entity);
                    commonDistributionConfigs.add(config);
                }
            });
        });
        return commonDistributionConfigs;
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

    private Map<Provider, List<NotificationContent>> getNotificationsPerProvider(final Collection<NotificationContent> unfilteredNotifications) {
        final Map<Provider, List<NotificationContent>> providerToNotifications = new HashMap<>();
        for (ProviderDescriptor providerDescriptor : providerDescriptors) {
            final List<NotificationContent> providerNotifications = unfilteredNotifications
                                                                        .parallelStream()
                                                                        .filter(content -> providerDescriptor.getName().equals(content.getProvider()))
                                                                        .collect(Collectors.toList());
            providerToNotifications.put(providerDescriptor.getProvider(), providerNotifications);
        }
        return providerToNotifications;
    }

    private Predicate<NotificationContent> createFilter(Collection<HierarchicalField> filterableFields, final Collection<CommonDistributionConfig> distributionConfigs) {
        JsonFilterBuilder filterBuilder = DefaultFilterBuilders.ALWAYS_TRUE;
        for (final CommonDistributionConfig config : distributionConfigs) {
            for (final HierarchicalField field : filterableFields) {
                Collection<String> valuesFromField = getValuesFromConfig(field.getConfigNameMapping(), config);
                if (valuesFromField != null) {
                    final JsonFilterBuilder fieldFilter = createFilterBuilderForAllValues(field, valuesFromField);
                    filterBuilder = new AndFieldFilterBuilder(filterBuilder, fieldFilter);
                }
            }
        }
        return filterBuilder.buildPredicate();
    }

    private Collection<String> getValuesFromConfig(final String configKey, final CommonDistributionConfig config) {
        final JsonObject jsonConfig = gson.toJsonTree(config).getAsJsonObject();
        final JsonElement jsonElement = jsonConfig.get(configKey);

        List<String> values = null;
        if (jsonElement.isJsonPrimitive()) {
            return Arrays.asList(jsonElement.getAsString());
        } else if (jsonElement.isJsonArray()) {
            values = new ArrayList<>();
            for (JsonElement arrayElement : jsonElement.getAsJsonArray()) {
                values.add(arrayElement.getAsString());
            }
        }
        return values;
    }

    private JsonFilterBuilder createFilterBuilderForAllValues(final HierarchicalField field, final Collection<String> applicableValues) {
        JsonFilterBuilder filterBuilderForAllValues = DefaultFilterBuilders.ALWAYS_FALSE;

        for (final String value : applicableValues) {
            JsonFilterBuilder filterBuilderForValue = new JsonFieldFilter(gson, field, value);
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
}
