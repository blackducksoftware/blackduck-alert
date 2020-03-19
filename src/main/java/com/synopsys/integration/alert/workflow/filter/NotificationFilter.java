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
package com.synopsys.integration.alert.workflow.filter;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.ProviderContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.filter.builder.AndFieldFilterBuilder;
import com.synopsys.integration.alert.common.workflow.filter.builder.DefaultFilterBuilders;
import com.synopsys.integration.alert.common.workflow.filter.builder.JsonFilterBuilder;
import com.synopsys.integration.alert.common.workflow.filter.builder.OrFieldFilterBuilder;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;

@Component
public class NotificationFilter {
    private final ConfigurationAccessor jobConfigReader;
    private final JsonExtractor jsonExtractor;
    private final List<Provider> providers;

    @Autowired
    public NotificationFilter(final JsonExtractor jsonExtractor, final List<Provider> providers, final ConfigurationAccessor jobConfigReader) {
        this.jsonExtractor = jsonExtractor;
        this.providers = providers;
        this.jobConfigReader = jobConfigReader;
    }

    /**
     * Creates a java.util.Collection of NotificationContent objects that are applicable for at least one Distribution Job.
     * @return A java.util.List of sorted (by createdAt) NotificationContent objects.
     */
    public Collection<AlertNotificationWrapper> extractApplicableNotifications(final FrequencyType frequency, final Collection<AlertNotificationWrapper> notificationList) {
        final List<ConfigurationJobModel> unfilteredDistributionConfigs = jobConfigReader.getAllJobs();
        if (unfilteredDistributionConfigs.isEmpty()) {
            return List.of();
        }

        final List<ConfigurationJobModel> distributionConfigs = unfilteredDistributionConfigs
                                                                    .parallelStream()
                                                                    .filter(config -> frequency.equals(config.getFrequencyType()))
                                                                    .collect(Collectors.toList());

        if (distributionConfigs.isEmpty()) {
            return List.of();
        }

        final Set<String> configuredNotificationTypes = getConfiguredNotificationTypes(distributionConfigs);
        if (configuredNotificationTypes.isEmpty()) {
            return List.of();
        }

        final List<ProviderContentType> providerContentTypes = getProviderContentTypes();
        final Map<String, List<AlertNotificationWrapper>> notificationsByType = getNotificationsByType(configuredNotificationTypes, notificationList);

        final Set<AlertNotificationWrapper> filteredNotifications = new HashSet<>();
        for (final Map.Entry<String, List<AlertNotificationWrapper>> notificationByType : notificationsByType.entrySet()) {
            final Set<JsonField> filterableFields = getFilterableFieldsByNotificationType(notificationByType.getKey(), providerContentTypes);
            final Predicate<AlertNotificationWrapper> filterForNotificationType = createFilter(filterableFields, distributionConfigs);

            final List<AlertNotificationWrapper> matchingNotifications = applyFilter(notificationByType.getValue(), filterForNotificationType);
            filteredNotifications.addAll(matchingNotifications);
        }
        return filteredNotifications
                   .parallelStream()
                   .sorted(Comparator.comparing(AlertNotificationWrapper::getCreatedAt))
                   .collect(Collectors.toList());
    }

    /**
     * Creates a java.util.Collection of NotificationContent objects that are applicable for at least one Distribution Job.
     * @return A java.util.List of sorted (by createdAt) NotificationContent objects.
     */
    public Collection<AlertNotificationWrapper> extractApplicableNotifications(final Set<ProviderContentType> providerContentTypes, final ConfigurationJobModel jobConfiguration,
        final Collection<AlertNotificationWrapper> notificationList) {
        final Set<String> configuredNotificationTypes = new HashSet<>(jobConfiguration.getNotificationTypes());
        if (configuredNotificationTypes.isEmpty()) {
            return List.of();
        }

        final Map<String, List<AlertNotificationWrapper>> notificationsByType = getNotificationsByType(configuredNotificationTypes, notificationList);

        final Set<AlertNotificationWrapper> filteredNotifications = new HashSet<>();
        notificationsByType.forEach((type, groupedNotifications) -> {
            final Set<JsonField> filterableFields = getFilterableFieldsByNotificationType(type, providerContentTypes);
            final Predicate<AlertNotificationWrapper> filterForNotificationType = createJobFilter(filterableFields, jobConfiguration);

            final List<AlertNotificationWrapper> matchingNotifications = applyFilter(groupedNotifications, filterForNotificationType);
            filteredNotifications.addAll(matchingNotifications);
        });
        return filteredNotifications
                   .parallelStream()
                   .sorted(Comparator.comparing(AlertNotificationWrapper::getProviderCreationTime))
                   .collect(Collectors.toList());
    }

    private <T> List<T> applyFilter(final Collection<T> notificationList, final Predicate<T> filter) {
        return notificationList
                   .parallelStream()
                   .filter(filter)
                   .collect(Collectors.toList());
    }

    private Set<String> getConfiguredNotificationTypes(final List<ConfigurationJobModel> distributionConfigs) {
        return distributionConfigs
                   .parallelStream()
                   .flatMap(config -> config.getNotificationTypes().stream())
                   .collect(Collectors.toSet());
    }

    private Map<String, List<AlertNotificationWrapper>> getNotificationsByType(final Set<String> notificationTypes, final Collection<AlertNotificationWrapper> notifications) {
        final Map<String, List<AlertNotificationWrapper>> notificationsByType = new HashMap<>();
        notificationTypes.parallelStream().forEach(type -> {
            final List<AlertNotificationWrapper> applicableNotifications = applyFilter(notifications, notification -> type.equals(notification.getNotificationType()));
            notificationsByType.put(type, applicableNotifications);
        });
        return notificationsByType;
    }

    // TODO since this is used with the MessageContentAggregator we don't need to iterate again; this can be removed
    private List<ProviderContentType> getProviderContentTypes() {
        return providers
                   .parallelStream()
                   .map(Provider::getProviderContent)
                   .map(ProviderContent::getContentTypes)
                   .flatMap(Set::stream)
                   .collect(Collectors.toList());
    }

    private Set<JsonField> getFilterableFieldsByNotificationType(final String notificationType, final Collection<ProviderContentType> contentTypes) {
        return contentTypes
                   .parallelStream()
                   .filter(contentType -> notificationType.equals(contentType.getNotificationType()))
                   .flatMap(contentType -> contentType.getFilterableFields().stream())
                   .collect(Collectors.toSet());
    }

    private Predicate<AlertNotificationWrapper> createFilter(final Collection<JsonField> filterableFields, final Collection<ConfigurationJobModel> distributionConfigs) {
        if (filterableFields.isEmpty()) {
            return DefaultFilterBuilders.ALWAYS_TRUE.buildPredicate();
        }
        Predicate<AlertNotificationWrapper> orPredicate = DefaultFilterBuilders.ALWAYS_FALSE.buildPredicate();
        for (final ConfigurationJobModel config : distributionConfigs) {
            orPredicate = orPredicate.or(createJobFilter(filterableFields, config));
        }
        return orPredicate;
    }

    private Predicate<AlertNotificationWrapper> createJobFilter(final Collection<JsonField> filterableFields, final ConfigurationJobModel config) {
        JsonFilterBuilder filterBuilder = DefaultFilterBuilders.ALWAYS_TRUE;
        for (final JsonField field : filterableFields) {
            final JsonFilterBuilder fieldFilter = createFieldFilter(field, config);
            filterBuilder = new AndFieldFilterBuilder(filterBuilder, fieldFilter);
        }
        return filterBuilder.buildPredicate();
    }

    private JsonFilterBuilder createFieldFilter(final JsonField field, final ConfigurationJobModel config) {
        JsonFilterBuilder filterBuilder = DefaultFilterBuilders.ALWAYS_TRUE;

        final List<String> configKeyMappings = field.getConfigKeyMappings();
        final Optional<ConfigurationFieldModel> optionalConditionalField = getConditionalField(configKeyMappings, config);
        if (optionalConditionalField.isPresent()) {
            final boolean isConditionMet = optionalConditionalField
                                               .flatMap(ConfigurationFieldModel::getFieldValue)
                                               .filter(BooleanUtils::toBoolean)
                                               .isPresent();
            if (isConditionMet) {
                final ConfigurationFieldModel conditionalField = optionalConditionalField.get();
                final List<String> relevantKeys = configKeyMappings
                                                      .stream()
                                                      .filter(mapping -> !mapping.equals(conditionalField.getFieldKey()))
                                                      .collect(Collectors.toList());
                final JsonFilterBuilder newFilterBuilder = createFilterBuilderForKeys(relevantKeys, field, config);
                filterBuilder = new AndFieldFilterBuilder(filterBuilder, newFilterBuilder);
            }
        } else {
            // There is no conditional, but the config is still filterable.
            final JsonFilterBuilder newFilterBuilder = createFilterBuilderForKeys(configKeyMappings, field, config);
            filterBuilder = new AndFieldFilterBuilder(filterBuilder, newFilterBuilder);
        }
        return filterBuilder;
    }

    private JsonFilterBuilder createFilterBuilderForKeys(final List<String> relevantKeys, final JsonField field, final ConfigurationJobModel config) {
        JsonFilterBuilder filterBuilder = DefaultFilterBuilders.ALWAYS_FALSE;
        for (final String relevantKey : relevantKeys) {
            final JsonFilterBuilder fieldFilter = config
                                                      .getFieldAccessor()
                                                      .getField(relevantKey)
                                                      .map(ConfigurationFieldModel::getFieldValues)
                                                      .map(values -> createFilterBuilderForAllValues(field, values))
                                                      .orElse(DefaultFilterBuilders.ALWAYS_FALSE);
            filterBuilder = new OrFieldFilterBuilder(filterBuilder, fieldFilter);
        }
        return filterBuilder;
    }

    private JsonFilterBuilder createFilterBuilderForAllValues(final JsonField field, final Collection<String> applicableValues) {
        JsonFilterBuilder filterBuilderForAllValues = DefaultFilterBuilders.ALWAYS_FALSE;
        for (final String value : applicableValues) {
            final JsonFilterBuilder filterBuilderForValue = new JsonFieldFilterBuilder(jsonExtractor, field, value);
            filterBuilderForAllValues = new OrFieldFilterBuilder(filterBuilderForAllValues, filterBuilderForValue);
        }
        return filterBuilderForAllValues;
    }

    private Optional<ConfigurationFieldModel> getConditionalField(final List<String> configKeyMappings, final ConfigurationJobModel config) {
        for (final String configKey : configKeyMappings) {
            final Optional<ConfigurationFieldModel> optionalConfigField = config.getFieldAccessor()
                                                                              .getField(configKey)
                                                                              .filter(field -> field.getFieldKey().startsWith(ChannelDistributionUIConfig.KEY_COMMON_CHANNEL_PREFIX));
            if (optionalConfigField.isPresent()) {
                final ConfigurationFieldModel configField = optionalConfigField.get();
                final Collection<String> values = configField.getFieldValues();
                final boolean isBooleanField = values
                                                   .stream()
                                                   .allMatch(this::isBoolean);
                if (isBooleanField) {
                    return optionalConfigField;
                }
            }
        }
        return Optional.empty();
    }

    private boolean isBoolean(final String value) {
        return StringUtils.isNotBlank(value) && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"));
    }

}
