/*
 * api-provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.provider;

import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
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
    public static final String KEY_FILTER_BY_PROJECT = ChannelDistributionUIConfig.KEY_COMMON_CHANNEL_PREFIX + "filter.by.project";
    public static final String KEY_PROJECT_NAME_PATTERN = ChannelDistributionUIConfig.KEY_COMMON_CHANNEL_PREFIX + "project.name.pattern";
    public static final String KEY_CONFIGURED_PROJECT = ChannelDistributionUIConfig.KEY_COMMON_CHANNEL_PREFIX + "configured.project";

    public static final String LABEL_FILTER_BY_PROJECT = "Filter By Project";
    public static final String LABEL_PROJECT_NAME_PATTERN = "Project Name Pattern";
    public static final String LABEL_PROJECTS = "Projects";
    public static final String LABEL_NOTIFICATION_TYPES = "Notification Types";
    public static final String LABEL_PROCESSING = "Processing";

    public static final String DESCRIPTION_FILTER_BY_PROJECT = "If selected, only notifications from the selected Projects table will be processed. Otherwise notifications from all Projects are processed.";
    public static final String DESCRIPTION_PROJECT_NAME_PATTERN = "The regular expression to use to determine what Projects to include. These are in addition to the Projects selected in the table.";
    public static final String DESCRIPTION_PROJECTS = "Select a project or projects that will be used to retrieve notifications from your provider.";
    public static final String DESCRIPTION_NOTIFICATION_TYPES = "Select one or more of the notification types. Only these notification types will be included for this distribution job.";
    public static final String DESCRIPTION_PROCESSING = "Select the way messages will be processed: ";
    public static final String DESCRIPTION_PROVIDER_CONFIG_FIELD = "The provider configuration to use with this distribution job.";

    public ProviderDescriptor(ProviderKey providerKey, ProviderGlobalUIConfig providerUiConfig) {
        super(providerKey, DescriptorType.PROVIDER);
        addGlobalUiConfig(providerUiConfig);
    }

}
