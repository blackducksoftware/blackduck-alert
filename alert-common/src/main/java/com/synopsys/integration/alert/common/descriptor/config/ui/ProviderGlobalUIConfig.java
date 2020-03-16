/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.provider.ProviderKey;

public abstract class ProviderGlobalUIConfig extends UIConfig {

    private final ProviderKey providerKey;

    public ProviderGlobalUIConfig(ProviderKey providerKey, String label, String description, String urlName) {
        super(label, description, urlName);
        this.providerKey = providerKey;
    }

    @Override
    public final List<ConfigField> createFields() {
        ConfigField providerConfigEnabled = new CheckboxConfigField(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED, ProviderDescriptor.LABEL_PROVIDER_CONFIG_ENABLED, ProviderDescriptor.DESCRIPTION_PROVIDER_CONFIG_ENABLED)
                                                .applyDefaultValue(Boolean.TRUE.toString());
        ConfigField providerConfigName = new TextInputConfigField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, ProviderDescriptor.LABEL_PROVIDER_CONFIG_NAME, ProviderDescriptor.DESCRIPTION_PROVIDER_CONFIG_NAME).applyRequired(true);

        List<ConfigField> providerCommonGlobalFields = List.of(providerConfigEnabled, providerConfigName);
        List<ConfigField> providerGlobalFields = createProviderGlobalFields();
        return Streams.concat(providerCommonGlobalFields.stream(), providerGlobalFields.stream()).collect(Collectors.toList());
    }

    public abstract List<ConfigField> createProviderGlobalFields();

    public ProviderKey getProviderKey() {
        return providerKey;
    }

}
