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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.RestApi;
import com.synopsys.integration.alert.common.descriptor.config.UIComponent;
import com.synopsys.integration.alert.common.enumeration.RestApiTypes;
import com.synopsys.integration.alert.common.exception.AlertException;

@Component
public class DescriptorMap {
    private final Map<String, Descriptor> descriptorMap;
    private final Map<String, ChannelDescriptor> channelDescriptorMap;
    private final Map<String, ProviderDescriptor> providerDescriptorMap;
    private final Map<String, ComponentDescriptor> componentDescriptorMap;
    private final List<RestApi> restApis;

    @Autowired
    public DescriptorMap(final List<ChannelDescriptor> channelDescriptors, final List<ProviderDescriptor> providerDescriptors, final List<ComponentDescriptor> componentDescriptors, final List<RestApi> restApis)
            throws AlertException {
        this.restApis = restApis;
        descriptorMap = new HashMap<>(channelDescriptors.size() + providerDescriptors.size());
        channelDescriptorMap = initDescriptorMap(channelDescriptors);
        providerDescriptorMap = initDescriptorMap(providerDescriptors);
        componentDescriptorMap = initDescriptorMap(componentDescriptors);
    }

    private <D extends Descriptor> Map<String, D> initDescriptorMap(final List<D> descriptorList) throws AlertException {
        final Map<String, D> descriptorMapping = new HashMap<>(descriptorList.size());
        for (final D descriptor : descriptorList) {
            final String descriptorName = descriptor.getName();
            if (descriptorMap.containsKey(descriptorName)) {
                throw new AlertException("Found duplicate descriptor name of: " + descriptorName);
            }
            descriptorMap.put(descriptorName, descriptor);
            descriptorMapping.put(descriptorName, descriptor);
        }
        return descriptorMapping;
    }

    public List<RestApi> getStartupRestApis() {
        return restApis
                .stream()
                .filter(descriptorConfig -> descriptorConfig.hasStartupProperties())
                .collect(Collectors.toList());
    }

    public List<UIComponent> getDistributionUIConfigs() {
        return getUIComponents(RestApiTypes.CHANNEL_DISTRIBUTION_CONFIG);
    }

    public List<UIComponent> getGlobalUIConfigs() {
        return getUIComponents(RestApiTypes.CHANNEL_GLOBAL_CONFIG);
    }

    public List<UIComponent> getProviderUIConfigs() {
        return getUIComponents(RestApiTypes.PROVIDER_CONFIG);
    }

    public List<UIComponent> getComponentUIConfigs() {
        return getUIComponents(RestApiTypes.COMPONENT_CONFIG);
    }

    public List<UIComponent> getUIComponents(final RestApiTypes configType) {
        return descriptorMap.values()
                .stream()
                .filter(descriptor -> descriptor.getUIConfig(configType) != null)
                .map(descriptor -> descriptor.getUIConfig(configType).generateUIComponent())
                .collect(Collectors.toList());
    }

    public List<RestApi> getAllDescriptorConfigs() {
        return restApis;
    }

    public Descriptor getDescriptor(final String name) {
        return descriptorMap.get(name);
    }

    public ChannelDescriptor getChannelDescriptor(final String name) {
        return channelDescriptorMap.get(name);
    }

    public ProviderDescriptor getProviderDescriptor(final String name) {
        return providerDescriptorMap.get(name);
    }

    public ComponentDescriptor getComponentDescriptor(final String name) {
        return componentDescriptorMap.get(name);
    }

    public Map<String, Descriptor> getDescriptorMap() {
        return descriptorMap;
    }

    public Map<String, ChannelDescriptor> getChannelDescriptorMap() {
        return channelDescriptorMap;
    }

    public Map<String, ProviderDescriptor> getProviderDescriptorMap() {
        return providerDescriptorMap;
    }

    public Map<String, ComponentDescriptor> getComponentDescriptorMap() {
        return componentDescriptorMap;
    }
}
