/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor;

import java.util.Set;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public abstract class ChannelDescriptor extends Descriptor {
    public static final String KEY_COMMON_CHANNEL_PREFIX = "channel.common.";

    public static final String KEY_FREQUENCY = KEY_COMMON_CHANNEL_PREFIX + "frequency";
    public static final String KEY_PROVIDER_TYPE = KEY_COMMON_CHANNEL_PREFIX + "provider.name";
    public static final String KEY_CHANNEL_NAME = KEY_COMMON_CHANNEL_PREFIX + "channel.name";
    public static final String KEY_NAME = KEY_COMMON_CHANNEL_PREFIX + "name";
    public static final String KEY_ENABLED = KEY_COMMON_CHANNEL_PREFIX + "enabled";

    public static final String LABEL_ENABLED = "Enabled";
    public static final String LABEL_NAME = "Name";
    public static final String LABEL_FREQUENCY = "Frequency";
    public static final String LABEL_CHANNEL_NAME = "Channel Type";
    public static final String LABEL_PROVIDER_TYPE = "Provider Type";

    public ChannelDescriptor(ChannelKey channelKey, Set<ConfigContextEnum> configContexts) {
        super(channelKey, DescriptorType.CHANNEL, configContexts);
    }

}
