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
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;

public abstract class CommonDistributionUIConfig extends CommonFieldUIConfig {
    public static final String KEY_NAME = "channel.common.name";
    public static final String KEY_CHANNEL_NAME = "channel.common.channel.name";
    public static final String KEY_PROVIDER_NAME = "channel.common.provider.name";
    public static final String KEY_FREQUENCY = "channel.common.frequency";
    public static final String KEY_FILTER_BY_PROJECT = "channel.common.filter.by.project";
    public static final String KEY_PROJECT_NAME_PATTERN = "channel.common.project.name.pattern";
    public static final String KEY_CONFIGURED_PROJECT = "channel.common.configured.project";

    private final Set<String> channelDescriptors;
    private final Set<String> providerDescriptors;

    public CommonDistributionUIConfig(final BaseDescriptorAccessor descriptorAccessor) {
        channelDescriptors = findDescriptorNames(descriptorAccessor, DescriptorType.CHANNEL);
        providerDescriptors = findDescriptorNames(descriptorAccessor, DescriptorType.PROVIDER);

    }

    @Override
    public List<ConfigField> createCommonConfigFields() {
        final ConfigField name = new TextInputConfigField(KEY_NAME, "Name", true, false);
        final ConfigField frequency = new SelectConfigField(KEY_FREQUENCY, "Frequency", true, false, Arrays.stream(FrequencyType.values()).map(type -> type.getDisplayName()).collect(Collectors.toList()));
        final ConfigField channelName = new SelectConfigField(KEY_CHANNEL_NAME, "Channel Type", true, false, channelDescriptors);
        final ConfigField providerName = new SelectConfigField(KEY_PROVIDER_NAME, "Provider Type", true, false, providerDescriptors);

        return Arrays.asList(name, channelName, frequency, providerName);
    }

    private Set<String> findDescriptorNames(final BaseDescriptorAccessor descriptorAccessor, final DescriptorType descriptorType) {
        try {
            return descriptorAccessor.getRegisteredDescriptorsByType(descriptorType.name()).stream()
                       .map(registeredDescriptorModel -> registeredDescriptorModel.getName())
                       .collect(Collectors.toSet());
        } catch (final AlertDatabaseConstraintException e) {
            return Collections.emptySet();
        }
    }
}
