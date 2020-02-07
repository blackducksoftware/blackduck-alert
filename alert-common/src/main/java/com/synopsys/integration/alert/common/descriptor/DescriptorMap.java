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
package com.synopsys.integration.alert.common.descriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertException;

@Component
public class DescriptorMap {
    private final Map<String, Descriptor> descriptorMapping;
    private final Map<String, ChannelDescriptor> channelDescriptorMapping;
    private final Map<String, ProviderDescriptor> providerDescriptorMapping;
    private final Map<String, ComponentDescriptor> componentDescriptorMapping;

    @Autowired
    public DescriptorMap(final List<ChannelDescriptor> channelDescriptors, final List<ProviderDescriptor> providerDescriptors, final List<ComponentDescriptor> componentDescriptors)
        throws AlertException {
        descriptorMapping = new HashMap<>();
        channelDescriptorMapping = initDescriptorMap(channelDescriptors);
        providerDescriptorMapping = initDescriptorMap(providerDescriptors);
        componentDescriptorMapping = initDescriptorMap(componentDescriptors);
    }

    public Optional<Descriptor> getDescriptor(final String name) {
        return Optional.ofNullable(descriptorMapping.get(name));
    }

    public Set<Descriptor> getDescriptorByType(final DescriptorType descriptorType) {
        return descriptorMapping.entrySet().stream()
                   .map(Map.Entry::getValue)
                   .filter(descriptor -> descriptor.getType() == descriptorType)
                   .collect(Collectors.toSet());
    }

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
        final Map<String, D> specificDescriptorMapping = new HashMap<>(descriptorList.size());
        for (final D descriptor : descriptorList) {
            final String descriptorName = descriptor.getName();
            if (descriptorMapping.containsKey(descriptorName)) {
                throw new AlertException("Found duplicate descriptor name of: " + descriptorName);
            }
            descriptorMapping.put(descriptorName, descriptor);
            specificDescriptorMapping.put(descriptorName, descriptor);
        }
        return specificDescriptorMapping;
    }
}
