package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;

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

    public CommonDistributionUIConfig(final DescriptorMap descriptorMap) {
        channelDescriptors = descriptorMap.getChannelDescriptorMap().keySet();
        providerDescriptors = descriptorMap.getProviderDescriptorMap().keySet();
    }

    @Override
    public List<ConfigField> createCommonConfigFields() {
        final ConfigField name = new TextInputConfigField(KEY_NAME, "Name", true, false);
        final ConfigField frequency = new SelectConfigField(KEY_FREQUENCY, "Frequency", true, false, Arrays.stream(FrequencyType.values()).map(type -> type.getDisplayName()).collect(Collectors.toList()));
        final ConfigField channelName = new SelectConfigField(KEY_CHANNEL_NAME, "Channel Type", true, false, channelDescriptors);
        final ConfigField providerName = new SelectConfigField(KEY_PROVIDER_NAME, "Provider Type", true, false, providerDescriptors);

        return Arrays.asList(name, channelName, frequency, providerName);
    }
}
