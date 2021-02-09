/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.descriptor;

import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderGlobalUIConfig;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.provider.ProviderKey;

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

}
