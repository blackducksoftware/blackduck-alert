/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointSelectField;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

public abstract class ChannelDistributionUIConfig extends UIConfig {
    public static final String KEY_COMMON_CHANNEL_PREFIX = "channel.common.";

    public static final String KEY_ENABLED = KEY_COMMON_CHANNEL_PREFIX + "enabled";
    public static final String KEY_NAME = KEY_COMMON_CHANNEL_PREFIX + "name";
    public static final String KEY_CHANNEL_NAME = KEY_COMMON_CHANNEL_PREFIX + "channel.name";
    public static final String KEY_PROVIDER_NAME = KEY_COMMON_CHANNEL_PREFIX + "provider.name";
    public static final String KEY_FREQUENCY = KEY_COMMON_CHANNEL_PREFIX + "frequency";

    private static final String LABEL_ENABLED = "Enabled";
    private static final String LABEL_NAME = "Name";
    private static final String LABEL_FREQUENCY = "Frequency";
    private static final String LABEL_CHANNEL_NAME = "Channel Type";
    private static final String LABEL_PROVIDER_NAME = "Provider Type";

    private static final String DESCRIPTION_ENABLED = "If selected, this job will be used for processing provider notifications, otherwise, this job will not be used.";
    private static final String DESCRIPTION_NAME = "The name of the distribution job. Must be unique.";
    private static final String DESCRIPTION_FREQUENCY = "Select how frequently this job should check for notifications to send.";
    private static final String DESCRIPTION_CHANNEL_NAME = "Select the channel. Notifications generated through Alert will be sent through this channel.";
    private static final String DESCRIPTION_PROVIDER_NAME = "Select the provider. Only notifications for that provider will be processed in this distribution job.";

    private final ChannelKey channelKey;
    private final ProviderKey defaultProviderKey;

    public ChannelDistributionUIConfig(ChannelKey channelKey, String label, String urlName) {
        super(label, "Channel distribution setup.", urlName);
        this.channelKey = channelKey;
        this.defaultProviderKey = new BlackDuckProviderKey();
    }

    @Override
    public List<ConfigField> createFields() {
        ConfigField enabled = new CheckboxConfigField(KEY_ENABLED, LABEL_ENABLED, DESCRIPTION_ENABLED).applyDefaultValue(Boolean.TRUE.toString());
        ConfigField channelNameField = new EndpointSelectField(KEY_CHANNEL_NAME, LABEL_CHANNEL_NAME, DESCRIPTION_CHANNEL_NAME)
                                           .applyClearable(false)
                                           .applyRequired(true);
        ConfigField name = new TextInputConfigField(KEY_NAME, LABEL_NAME, DESCRIPTION_NAME).applyRequired(true);

        List<LabelValueSelectOption> frequencyOptions = Arrays.stream(FrequencyType.values())
                                                            .map(frequencyType -> new LabelValueSelectOption(frequencyType.getDisplayName(), frequencyType.name()))
                                                            .sorted()
                                                            .collect(Collectors.toList());
        ConfigField frequency = new SelectConfigField(KEY_FREQUENCY, LABEL_FREQUENCY, DESCRIPTION_FREQUENCY, frequencyOptions).applyRequired(true);
        ConfigField providerName = new EndpointSelectField(KEY_PROVIDER_NAME, LABEL_PROVIDER_NAME, DESCRIPTION_PROVIDER_NAME)
                                       .applyClearable(false)
                                       .applyRequired(true)
                                       .applyDefaultValue(defaultProviderKey.getUniversalKey());

        List<ConfigField> configFields = List.of(enabled, channelNameField, name, frequency, providerName);
        List<ConfigField> channelDistributionFields = createChannelDistributionFields();
        return Stream.concat(configFields.stream(), channelDistributionFields.stream()).collect(Collectors.toList());
    }

    public abstract List<ConfigField> createChannelDistributionFields();

    public ChannelKey getChannelKey() {
        return channelKey;
    }

}
