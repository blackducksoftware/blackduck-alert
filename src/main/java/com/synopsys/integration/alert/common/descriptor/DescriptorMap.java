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

import com.synopsys.integration.alert.common.enumeration.DescriptorConfigType;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorConfig;
import com.synopsys.integration.alert.common.exception.AlertException;

@Component
public class DescriptorMap {
    private final Map<String, Descriptor> descriptorMap;
    private final Map<String, ChannelDescriptor> channelDescriptorMap;
    private final Map<String, ProviderDescriptor> providerDescriptorMap;
    private final List<DescriptorConfig> descriptorConfigs;

    @Autowired
    public DescriptorMap(final List<ChannelDescriptor> channelDescriptors, final List<ProviderDescriptor> providerDescriptors, final List<DescriptorConfig> descriptorConfigs) throws AlertException {
        this.descriptorConfigs = descriptorConfigs;
        descriptorMap = new HashMap<>(channelDescriptors.size() + providerDescriptors.size());
        channelDescriptorMap = initMap(channelDescriptors);
        providerDescriptorMap = initMap(providerDescriptors);
    }

    private <D extends Descriptor> Map<String, D> initMap(final List<D> descriptorList) throws AlertException {
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

    public List<DescriptorConfig> getStartupDescriptorConfigs() {
        return descriptorConfigs
                       .stream()
                       .filter(descriptorConfig -> descriptorConfig.hasStartupProperties())
                       .collect(Collectors.toList());
    }

    public List<DescriptorConfig> getDistributionDescriptorConfigs() {
        return getDescriptorConfigs(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
    }

    public List<DescriptorConfig> getGlobalDescriptorConfigs() {
        return getDescriptorConfigs(DescriptorConfigType.CHANNEL_GLOBAL_CONFIG);
    }

    public List<DescriptorConfig> getProviderDescriptorConfigs() {
        return getDescriptorConfigs(DescriptorConfigType.PROVIDER_CONFIG);
    }

    public List<DescriptorConfig> getDescriptorConfigs(final DescriptorConfigType configType) {
        return descriptorMap.values()
                       .stream()
                       .filter(descriptor -> descriptor.getConfig(configType) != null)
                       .map(descriptor -> descriptor.getConfig(configType))
                       .collect(Collectors.toList());
    }

    public List<DescriptorConfig> getAllDescriptorConfigs() {
        return descriptorConfigs;
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

    public Map<String, Descriptor> getDescriptorMap() {
        return descriptorMap;
    }

    public Map<String, ChannelDescriptor> getChannelDescriptorMap() {
        return channelDescriptorMap;
    }

    public Map<String, ProviderDescriptor> getProviderDescriptorMap() {
        return providerDescriptorMap;
    }
}
