/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ActionApiType;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class Descriptor {
    private final String name;
    private final DescriptorType type;
    private final Map<ActionApiType, DescriptorActionApi> restApis;
    private final Map<ActionApiType, UIConfig> uiConfigs;

    public Descriptor(final String name, final DescriptorType type) {
        this.name = name;
        this.type = type;
        restApis = new HashMap<>();
        uiConfigs = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public DescriptorType getType() {
        return type;
    }

    public void addProviderActionApi(final DescriptorActionApi descriptorActionApi) {
        restApis.put(ActionApiType.PROVIDER_CONFIG, descriptorActionApi);
    }

    public void addGlobalActionApi(final DescriptorActionApi descriptorActionApi) {
        restApis.put(ActionApiType.CHANNEL_GLOBAL_CONFIG, descriptorActionApi);
    }

    public void addChannelDistributionActionApi(final DescriptorActionApi descriptorActionApi) {
        restApis.put(ActionApiType.CHANNEL_DISTRIBUTION_CONFIG, descriptorActionApi);
    }

    public void addProviderDistributionActionApi(final DescriptorActionApi descriptorActionApi) {
        restApis.put(ActionApiType.PROVIDER_DISTRIBUTION_CONFIG, descriptorActionApi);
    }

    public void addComponentActionApi(final DescriptorActionApi descriptorActionApi) {
        restApis.put(ActionApiType.COMPONENT_CONFIG, descriptorActionApi);
    }

    public void addProviderUiConfigs(final DescriptorActionApi descriptorActionApi, final UIConfig uiConfig) {
        uiConfigs.put(ActionApiType.PROVIDER_CONFIG, uiConfig);
        addProviderActionApi(descriptorActionApi);
    }

    public void addGlobalUiConfigs(final DescriptorActionApi descriptorActionApi, final UIConfig uiConfig) {
        uiConfigs.put(ActionApiType.CHANNEL_GLOBAL_CONFIG, uiConfig);
        addGlobalActionApi(descriptorActionApi);
    }

    public void addChannelDistributionUiConfigs(final DescriptorActionApi descriptorActionApi, final CommonDistributionUIConfig uiConfig) {
        uiConfigs.put(ActionApiType.CHANNEL_DISTRIBUTION_CONFIG, uiConfig);
        addChannelDistributionActionApi(descriptorActionApi);
    }

    public void addProviderDistributionUiConfigs(final DescriptorActionApi descriptorActionApi, final ProviderDistributionUIConfig uiConfig) {
        uiConfigs.put(ActionApiType.PROVIDER_DISTRIBUTION_CONFIG, uiConfig);
        addProviderDistributionActionApi(descriptorActionApi);
    }

    public void addComponentUiConfigs(final DescriptorActionApi descriptorActionApi, final UIConfig uiConfig) {
        uiConfigs.put(ActionApiType.COMPONENT_CONFIG, uiConfig);
        addComponentActionApi(descriptorActionApi);
    }

    public DescriptorActionApi getRestApi(final ActionApiType actionApiType) {
        return restApis.get(actionApiType);
    }

    public UIConfig getUIConfig(final ActionApiType actionApiType) {
        return uiConfigs.get(actionApiType);
    }

    public List<UIConfig> getAllUIConfigs() {
        if (hasUIConfigs()) {
            return uiConfigs.values().stream().collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public boolean hasUIConfigs() {
        return uiConfigs.size() > 0;
    }

    public boolean hasUIConfigForType(final ActionApiType actionApiType) {
        return uiConfigs.containsKey(actionApiType);
    }

    public void validateConfig(final ActionApiType actionApiType, final FieldAccessor fieldAccessor, final Map<String, String> fieldErrors) {
        getRestApi(actionApiType).validateConfig(fieldAccessor, fieldErrors);
    }

    public void testConfig(final ActionApiType actionApiType, final TestConfigModel testConfig) throws IntegrationException {
        getRestApi(actionApiType).testConfig(testConfig);
    }

}
