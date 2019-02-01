/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.common.configuration;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationJobModel;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;

public class CommonDistributionConfiguration extends Configuration {
    private final String name;
    private final String channelName;
    private final String providerName;
    private final FrequencyType frequencyType;
    private final FormatType formatType;
    private final Set<String> notificationTypes;
    // FIXME this field is here temporarily as there is some tight coupling to the BD provider when filtering (NotificationFilter).
    private final Boolean filterByProject;
    // FIXME this field is here temporarily as there is some tight coupling to the BD provider.
    private final String projectNamePattern;
    // FIXME this field is here temporarily as there is some tight coupling to the BD provider.
    private final Set<String> configuredProjects;
    private final UUID id;

    public CommonDistributionConfiguration(final ConfigurationJobModel configurationJobModel) {
        this(configurationJobModel.getJobId(), configurationJobModel.createKeyToFieldMap());
    }

    public CommonDistributionConfiguration(@NotNull final UUID jobUUID, @NotNull final Map<String, ConfigurationFieldModel> keyToFieldMap) {
        super(keyToFieldMap);

        id = jobUUID;
        name = getFieldAccessor().getString(ChannelDistributionUIConfig.KEY_NAME).orElse(null);
        channelName = getFieldAccessor().getString(ChannelDistributionUIConfig.KEY_CHANNEL_NAME).orElse(null);
        providerName = getFieldAccessor().getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).orElse(null);
        notificationTypes = getFieldAccessor().getAllStrings(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES).stream().collect(Collectors.toSet());
        frequencyType = getFieldAccessor().getEnum(ChannelDistributionUIConfig.KEY_FREQUENCY, FrequencyType.class).orElse(null);
        formatType = getFieldAccessor().getEnum(ProviderDistributionUIConfig.KEY_FORMAT_TYPE, FormatType.class).orElse(null);
        filterByProject = getFieldAccessor().getBoolean(BlackDuckDescriptor.KEY_FILTER_BY_PROJECT).orElse(null);
        projectNamePattern = getFieldAccessor().getString(BlackDuckDescriptor.KEY_PROJECT_NAME_PATTERN).orElse(null);
        configuredProjects = getFieldAccessor().getAllStrings(BlackDuckDescriptor.KEY_CONFIGURED_PROJECT).stream().collect(Collectors.toSet());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getProviderName() {
        return providerName;
    }

    public FrequencyType getFrequencyType() {
        return frequencyType;
    }

    public FormatType getFormatType() {
        return formatType;
    }

    public Collection<String> getNotificationTypes() {
        return notificationTypes;
    }

    public Boolean getFilterByProject() {
        return filterByProject;
    }

    public String getProjectNamePattern() {
        return projectNamePattern;
    }

    public Set<String> getConfiguredProjects() {
        return configuredProjects;
    }

}
