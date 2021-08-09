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

import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
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
    private final ChannelKey channelKey;
    private final ProviderKey defaultProviderKey;

    public ChannelDistributionUIConfig(ChannelKey channelKey, String label, String urlName) {
        super(label, "Channel distribution setup.", urlName);
        this.channelKey = channelKey;
        this.defaultProviderKey = new BlackDuckProviderKey();
    }

    @Override
    public List<ConfigField> createFields() {
        ConfigField enabled = new CheckboxConfigField(ChannelDescriptor.KEY_ENABLED, ChannelDescriptor.LABEL_ENABLED, ChannelDescriptor.DESCRIPTION_ENABLED).applyDefaultValue(Boolean.TRUE.toString());
        ConfigField channelNameField = new EndpointSelectField(ChannelDescriptor.KEY_CHANNEL_NAME, ChannelDescriptor.LABEL_CHANNEL_NAME, ChannelDescriptor.DESCRIPTION_CHANNEL_NAME)
            .applyClearable(false)
            .applyRequired(true);
        ConfigField name = new TextInputConfigField(ChannelDescriptor.KEY_NAME, ChannelDescriptor.LABEL_NAME, ChannelDescriptor.DESCRIPTION_NAME).applyRequired(true);

        List<LabelValueSelectOption> frequencyOptions = Arrays.stream(FrequencyType.values())
            .map(frequencyType -> new LabelValueSelectOption(frequencyType.getDisplayName(), frequencyType.name()))
            .sorted()
            .collect(Collectors.toList());
        ConfigField frequency = new SelectConfigField(ChannelDescriptor.KEY_FREQUENCY, ChannelDescriptor.LABEL_FREQUENCY, ChannelDescriptor.DESCRIPTION_FREQUENCY, frequencyOptions).applyRequired(true);
        ConfigField providerName = new EndpointSelectField(ChannelDescriptor.KEY_PROVIDER_TYPE, ChannelDescriptor.LABEL_PROVIDER_TYPE, ChannelDescriptor.DESCRIPTION_PROVIDER_TYPE)
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
