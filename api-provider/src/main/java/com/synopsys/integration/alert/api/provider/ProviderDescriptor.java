/*
 * api-provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.provider;

import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationValidator;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

public abstract class ProviderDescriptor extends Descriptor {
    public static final String KEY_COMMON_PROVIDER_PREFIX = "provider.common.";
    public static final String KEY_PROVIDER_CONFIG_NAME = KEY_COMMON_PROVIDER_PREFIX + "config.name";
    public static final String KEY_PROVIDER_CONFIG_ID = KEY_COMMON_PROVIDER_PREFIX + "config.id";
    public static final String KEY_PROVIDER_CONFIG_ENABLED = KEY_COMMON_PROVIDER_PREFIX + "config.enabled";
    public static final String LABEL_PROVIDER_CONFIG_ENABLED = "Enabled";
    public static final String LABEL_PROVIDER_CONFIG_NAME = "Provider Configuration";
    public static final String DESCRIPTION_PROVIDER_CONFIG_ENABLED =
        "If selected, this provider configuration will be able to pull data into Alert and available to configure with distribution jobs, otherwise, it will not be available for those usages.";
    public static final String DESCRIPTION_PROVIDER_CONFIG_NAME = "The name of this provider configuration. Must be unique.";

    public ProviderDescriptor(ProviderKey providerKey, ProviderGlobalUIConfig providerUiConfig, ProviderDistributionUIConfig distributionUIConfig) {
        super(providerKey, DescriptorType.PROVIDER);
        addGlobalUiConfig(providerUiConfig);
        addDistributionUiConfig(distributionUIConfig);
    }

    public ProviderDescriptor(ProviderKey providerKey, ProviderGlobalUIConfig providerUiConfig, ProviderDistributionUIConfig distributionUIConfig, GlobalConfigurationValidator globalConfigurationValidator) {
        super(providerKey, DescriptorType.PROVIDER, globalConfigurationValidator);
        addGlobalUiConfig(providerUiConfig);
        addDistributionUiConfig(distributionUIConfig);
    }

}
