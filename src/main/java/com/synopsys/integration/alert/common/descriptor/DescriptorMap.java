/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;

@Component
public class DescriptorMap {
    private final Map<String, Descriptor> descriptorMapping;
    private final Map<String, ChannelDescriptor> channelDescriptorMapping;
    private final Map<String, ProviderDescriptor> providerDescriptorMapping;
    private final Map<String, ComponentDescriptor> componentDescriptorMapping;
    private final List<DescriptorActionApi> restApis;

    @Autowired
    public DescriptorMap(final List<ChannelDescriptor> channelDescriptors, final List<ProviderDescriptor> providerDescriptors, final List<ComponentDescriptor> componentDescriptors, final List<DescriptorActionApi> restApis)
        throws AlertException {
        this.restApis = restApis;
        descriptorMapping = new HashMap<>();
        channelDescriptorMapping = initDescriptorMap(channelDescriptors);
        providerDescriptorMapping = initDescriptorMap(providerDescriptors);
        componentDescriptorMapping = initDescriptorMap(componentDescriptors);
    }

    public List<DescriptorMetadata> getDistributionUIConfigs() {
        return getDescriptorMetadataList(ConfigContextEnum.DISTRIBUTION);
    }

    public List<DescriptorMetadata> getGlobalUIConfigs() {
        return getDescriptorMetadataList(ConfigContextEnum.GLOBAL);
    }

    public List<DescriptorMetadata> getDescriptorMetadataList(final ConfigContextEnum configType) {
        return descriptorMapping.values()
                   .stream()
                   .filter(descriptor -> descriptor.hasUIConfigForType(configType))
                   .map(descriptor -> descriptor.getMetaData(configType))
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .collect(Collectors.toList());
    }

    public List<DescriptorMetadata> getAllUIComponents() {
        return Arrays.stream(ConfigContextEnum.values())
                   .flatMap(type -> getDescriptorMetadataList(type).stream())
                   .collect(Collectors.toList());
    }

    public List<DescriptorActionApi> getAllDescriptorConfigs() {
        return restApis;
    }

    public Optional<Descriptor> getDescriptor(final String name) {
        return Optional.ofNullable(descriptorMapping.get(name));
    }

    //TODO a lot of these get methods may do better to return optionals

    public Optional<ChannelDescriptor> getChannelDescriptor(final String name) {
        return Optional.ofNullable(channelDescriptorMapping.get(name));
    }

    public Optional<ProviderDescriptor> getProviderDescriptor(final String name) {
        return Optional.ofNullable(providerDescriptorMapping.get(name));
    }

    public Optional<ComponentDescriptor> getComponentDescriptor(final String name) {
        return Optional.ofNullable(componentDescriptorMapping.get(name));
    }

    public Map<String, Descriptor> getDescriptorMap() {
        return descriptorMapping;
    }

    public Map<String, ChannelDescriptor> getChannelDescriptorMap() {
        return channelDescriptorMapping;
    }

    public Map<String, ProviderDescriptor> getProviderDescriptorMap() {
        return providerDescriptorMapping;
    }

    public Map<String, ComponentDescriptor> getComponentDescriptorMap() {
        return componentDescriptorMapping;
    }

    private <D extends Descriptor> Map<String, D> initDescriptorMap(final List<D> descriptorList) throws AlertException {
        final Map<String, D> descriptorMapping = new HashMap<>(descriptorList.size());
        for (final D descriptor : descriptorList) {
            final String descriptorName = descriptor.getName();
            if (this.descriptorMapping.containsKey(descriptorName)) {
                throw new AlertException("Found duplicate descriptor name of: " + descriptorName);
            }
            this.descriptorMapping.put(descriptorName, descriptor);
            descriptorMapping.put(descriptorName, descriptor);
        }
        return descriptorMapping;
    }
}
