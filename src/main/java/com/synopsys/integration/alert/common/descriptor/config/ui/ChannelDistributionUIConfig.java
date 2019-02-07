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
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;

public abstract class ChannelDistributionUIConfig extends UIConfig {
    public static final String KEY_NAME = "channel.common.name";
    public static final String KEY_CHANNEL_NAME = "channel.common.channel.name";
    public static final String KEY_PROVIDER_NAME = "channel.common.provider.name";
    public static final String KEY_FREQUENCY = "channel.common.frequency";
    private final Logger logger = LoggerFactory.getLogger(ChannelDistributionUIConfig.class);
    private final BaseConfigurationAccessor configurationAccessor;
    private final BaseDescriptorAccessor descriptorAccessor;

    public ChannelDistributionUIConfig(final String label, final String urlName, final String fontAwesomeIcon, final BaseConfigurationAccessor configurationAccessor, final BaseDescriptorAccessor descriptorAccessor) {
        super(label, urlName, fontAwesomeIcon);
        this.configurationAccessor = configurationAccessor;
        this.descriptorAccessor = descriptorAccessor;
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField name = TextInputConfigField.createRequired(KEY_NAME, "Name");
        final ConfigField frequency = SelectConfigField.createRequired(KEY_FREQUENCY, "Frequency", Arrays.stream(FrequencyType.values()).map(FrequencyType::getDisplayName).collect(Collectors.toList()), this::validateJobName);
        final ConfigField channelName = SelectConfigField.createRequired(KEY_CHANNEL_NAME, "Channel Type", getDescriptorNames(DescriptorType.CHANNEL));
        final ConfigField providerName = SelectConfigField.createRequired(KEY_PROVIDER_NAME, "Provider Type", getDescriptorNames(DescriptorType.PROVIDER));

        final List<ConfigField> configFields = List.of(name, channelName, frequency, providerName);
        final List<ConfigField> channelDistributionFields = createChannelDistributionFields();
        return Stream.concat(configFields.stream(), channelDistributionFields.stream()).collect(Collectors.toList());
    }

    public abstract List<ConfigField> createChannelDistributionFields();

    private Collection<String> getDescriptorNames(final DescriptorType descriptorType) {
        try {
            return descriptorAccessor.getRegisteredDescriptorsByType(descriptorType).stream().map(RegisteredDescriptorModel::getName).collect(Collectors.toSet());
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("There was an error when retrieving data from the DB when building fields.");
        }
        return Set.of();
    }

    private Collection<String> validateJobName(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final Collection<String> errors = new ArrayList<>(1);
        final String descriptorName = fieldModel.getDescriptorName();
        final String jobName = fieldToValidate.getValue().orElse(null);
        if (StringUtils.isNotBlank(jobName)) {
            try {
                final List<ConfigurationModel> configurations = configurationAccessor.getConfigurationsByDescriptorName(descriptorName);
                final Boolean foundDuplicateName = configurations.stream()
                                                       .map(configurationModel -> configurationModel.getField(ChannelDistributionUIConfig.KEY_NAME).orElse(null))
                                                       .filter(configurationFieldModel -> (null != configurationFieldModel) && configurationFieldModel.getFieldValue().isPresent())
                                                       .anyMatch(configurationFieldModel -> jobName.equals(configurationFieldModel.getFieldValue().get()));
                if (foundDuplicateName) {
                    errors.add("A distribution configuration with this name already exists.");
                }
            } catch (final AlertDatabaseConstraintException e) {
                logger.error("Could not retrieve distributions of {}", jobName);
            }

        } else {
            errors.add("Name cannot be blank.");
        }

        return errors;
    }

}
