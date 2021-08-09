/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor;

import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
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

    public static final String DESCRIPTION_ENABLED = "If selected, this job will be used for processing provider notifications, otherwise, this job will not be used.";
    public static final String DESCRIPTION_NAME = "The name of the distribution job. Must be unique.";
    public static final String DESCRIPTION_FREQUENCY = "Select how frequently this job should check for notifications to send.";
    public static final String DESCRIPTION_CHANNEL_NAME = "Select the channel. Notifications generated through Alert will be sent through this channel.";
    public static final String DESCRIPTION_PROVIDER_TYPE = "Select the provider. Only notifications for that provider will be processed in this distribution job.";

    public ChannelDescriptor(ChannelKey channelKey, UIConfig globalUIConfig) {
        super(channelKey, DescriptorType.CHANNEL);
        addGlobalUiConfig(globalUIConfig);
    }

}
