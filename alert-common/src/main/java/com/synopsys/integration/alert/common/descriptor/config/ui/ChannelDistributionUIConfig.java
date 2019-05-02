/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;

public abstract class ChannelDistributionUIConfig extends UIConfig {
    public static final String KEY_NAME = "channel.common.name";
    public static final String KEY_CHANNEL_NAME = "channel.common.channel.name";
    public static final String KEY_PROVIDER_NAME = "channel.common.provider.name";
    public static final String KEY_FREQUENCY = "channel.common.frequency";

    private static final String LABEL_NAME = "Name";
    private static final String LABEL_FREQUENCY = "Frequency";
    private static final String LABEL_CHANNEL_NAME = "Channel Type";
    private static final String LABEL_PROVIDER_NAME = "Provider Type";

    private static final String DESCRIPTION_NAME = "The name of the distribution job. Must be unique.";
    private static final String DESCRIPTION_FREQUENCY = "Select how frequently this job should check for notifications to send.";
    private static final String DESCRIPTION_CHANNEL_NAME = "Select the channel. Notifications generated through Alert will be sent through this channel.";
    private static final String DESCRIPTION_PROVIDER_NAME = "Select the provider. Only notifications for that provider will be processed in this distribution job.";

    private final Logger logger = LoggerFactory.getLogger(ChannelDistributionUIConfig.class);
    private final DescriptorAccessor descriptorAccessor;

    public ChannelDistributionUIConfig(final String label, final String urlName, final String fontAwesomeIcon, final DescriptorAccessor descriptorAccessor) {
        super(label, "Channel distribution setup.", urlName, fontAwesomeIcon);
        this.descriptorAccessor = descriptorAccessor;
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField channelName = SelectConfigField.createRequired(KEY_CHANNEL_NAME, LABEL_CHANNEL_NAME, DESCRIPTION_CHANNEL_NAME, getDescriptorNames(DescriptorType.CHANNEL))
                                            .hideField(KEY_NAME)
                                            .hideField(KEY_FREQUENCY)
                                            .hideField(KEY_PROVIDER_NAME);

        final ConfigField name = TextInputConfigField.createRequired(KEY_NAME, LABEL_NAME, DESCRIPTION_NAME);
        final ConfigField frequency = SelectConfigField.createRequired(KEY_FREQUENCY, LABEL_FREQUENCY, DESCRIPTION_FREQUENCY, Arrays.stream(FrequencyType.values())
                                                                                                                                  .map(frequencyType -> new LabelValueSelectOption(frequencyType.getDisplayName(), frequencyType.name()))
                                                                                                                                  .collect(Collectors.toList()));

        final ConfigField providerName = SelectConfigField.createRequired(KEY_PROVIDER_NAME, LABEL_PROVIDER_NAME, DESCRIPTION_PROVIDER_NAME, getDescriptorNames(DescriptorType.PROVIDER));

        final List<ConfigField> configFields = List.of(channelName, name, frequency, providerName);
        final List<ConfigField> channelDistributionFields = createChannelDistributionFields();
        channelDistributionFields.stream().map(ConfigField::getKey).forEach(channelName::hideField);
        return Stream.concat(configFields.stream(), channelDistributionFields.stream()).collect(Collectors.toList());
    }

    public abstract List<ConfigField> createChannelDistributionFields();

    private Collection<LabelValueSelectOption> getDescriptorNames(final DescriptorType descriptorType) {
        try {
            return descriptorAccessor.getRegisteredDescriptorsByType(descriptorType).stream()
                       .map(RegisteredDescriptorModel::getName)
                       .map(LabelValueSelectOption::new)
                       .collect(Collectors.toSet());
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("There was an error when retrieving data from the DB when building fields.");
        }
        return Set.of();
    }

}
