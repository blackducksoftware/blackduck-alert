/*
 * api-provider
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.provider;

import java.util.Set;

import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

public abstract class ProviderDescriptor extends Descriptor {
    // Global
    public static final String KEY_COMMON_PROVIDER_PREFIX = "provider.common.";
    public static final String KEY_PROVIDER_CONFIG_NAME = KEY_COMMON_PROVIDER_PREFIX + "config.name";
    public static final String KEY_PROVIDER_CONFIG_ID = KEY_COMMON_PROVIDER_PREFIX + "config.id";
    public static final String KEY_PROVIDER_CONFIG_ENABLED = KEY_COMMON_PROVIDER_PREFIX + "config.enabled";

    public static final String LABEL_PROVIDER_CONFIG_ENABLED = "Enabled";
    public static final String LABEL_PROVIDER_CONFIG_NAME = "Provider Configuration";
    public static final String DESCRIPTION_PROVIDER_CONFIG_ENABLED =
        "If selected, this provider configuration will be able to pull data into Alert and available to configure with distribution jobs, otherwise, it will not be available for those usages.";
    public static final String DESCRIPTION_PROVIDER_CONFIG_NAME = "The name of this provider configuration. Must be unique.";

    // Distribution
    public static final String KEY_COMMON_CONFIG_ID = "provider.common.config.id";
    public static final String KEY_NOTIFICATION_TYPES = "provider.distribution.notification.types";
    public static final String KEY_PROCESSING_TYPE = "provider.distribution.processing.type";
    public static final String KEY_FILTER_BY_PROJECT = ChannelDescriptor.KEY_COMMON_CHANNEL_PREFIX + "filter.by.project";
    public static final String KEY_PROJECT_NAME_PATTERN = ChannelDescriptor.KEY_COMMON_CHANNEL_PREFIX + "project.name.pattern";
    public static final String KEY_PROJECT_VERSION_NAME_PATTERN = ChannelDescriptor.KEY_COMMON_CHANNEL_PREFIX + "project.version.name.pattern";
    public static final String KEY_CONFIGURED_PROJECT = ChannelDescriptor.KEY_COMMON_CHANNEL_PREFIX + "configured.project";

    public static final String LABEL_PROJECTS = "Projects";
    public static final String LABEL_PROCESSING = "Processing";

    public ProviderDescriptor(ProviderKey providerKey) {
        super(providerKey, DescriptorType.PROVIDER, Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION));
    }

}
