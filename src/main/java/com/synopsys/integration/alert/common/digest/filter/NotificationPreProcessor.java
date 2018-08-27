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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.workflow.filter.AndFieldFilterBuilder;
import com.synopsys.integration.alert.workflow.filter.HierarchicalField;
import com.synopsys.integration.alert.workflow.filter.JsonFieldFilter;
import com.synopsys.integration.alert.workflow.filter.JsonFilterBuilder;
import com.synopsys.integration.alert.workflow.filter.OrFieldFilterBuilder;

@Component
public class NotificationPreProcessor {
    private final Gson gson;
    private final List<ProviderDescriptor> providerDescriptors;

    @Autowired
    public NotificationPreProcessor(final Gson gson, final List<ProviderDescriptor> providerDescriptors) {
        this.gson = gson;
        this.providerDescriptors = providerDescriptors;
    }

    public List<NotificationContent> process(final Collection<NotificationContent> notificationList) {
        List<NotificationContent> filteredNotifications = new ArrayList<>();

        final Map<Provider, List<NotificationContent>> providerToNotifications = getNotificationsPerProvider(notificationList);
        providerToNotifications.forEach((provider, providerNotifications) -> {
            provider.getProviderContentTypes().forEach(contentType -> {
                final List<NotificationContent> notificationsByType = getNotificationsByType(contentType.getNotificationType(), providerNotifications);
                if (!notificationsByType.isEmpty()) {
                    Predicate<NotificationContent> filterForNotificationType = createFilter(contentType);

                    final List<NotificationContent> matchingNotifications = applyFilter(notificationsByType, filterForNotificationType);
                    filteredNotifications.addAll(matchingNotifications);
                }
            });
        });

        // FIXME sort

        return filteredNotifications;
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

    private List<NotificationContent> getNotificationsByType(final String notificationType, final Collection<NotificationContent> providerNotifications) {
        return providerNotifications.stream().filter(notificationContent -> notificationType.equals(notificationContent.getNotificationType())).collect(Collectors.toList());
    }

    private Predicate<NotificationContent> createFilter(ProviderContentType contentType) {
        // FIXME Lookup configurations for notificationType
        final List<Map<String, List<String>>> matchingConfigurations = Collections.emptyList();
        if (matchingConfigurations.isEmpty()) {
            return x -> false;
        }

        final Collection<HierarchicalField> filterableFields = contentType.getFilterableFields();

        // FIXME we need a default true/false filter builder
        JsonFilterBuilder filterBuilder = null;
        for (final Map<String, List<String>> configuration : matchingConfigurations) {
            for (final HierarchicalField field : filterableFields) {
                if (configuration.containsKey(field.innermostFieldName())) {
                    final JsonFilterBuilder fieldFilter = createFilterBuilderForAllValues(field, configuration.get(field.innermostFieldName()));
                    filterBuilder = new AndFieldFilterBuilder(filterBuilder, fieldFilter);
                }
            }
        }
        return filterBuilder.buildPredicate();
    }

    private JsonFilterBuilder createFilterBuilderForAllValues(final HierarchicalField field, final List<String> applicableValues) {
        // FIXME we need a default true/false filter builder
        JsonFilterBuilder filterBuilderForAllValues = null;

        for (final String value : applicableValues) {
            JsonFilterBuilder filterBuilderForValue = new JsonFieldFilter(gson, field, value);
            filterBuilderForAllValues = new OrFieldFilterBuilder(filterBuilderForAllValues, filterBuilderForValue);
        }

        return filterBuilderForAllValues;
    }

    private List<NotificationContent> applyFilter(final Collection<NotificationContent> notificationList, final Predicate<NotificationContent> filter) {
        return notificationList
                   .parallelStream()
                   .filter(filter)
                   .collect(Collectors.toList());
    }
}
