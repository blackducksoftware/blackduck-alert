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
package com.blackducksoftware.integration.hub.alert.descriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DescriptorMap {
    private final Map<String, Descriptor> descriptorMap;
    private final Map<String, ChannelDescriptor> channelDescriptorMap;
    private final Map<String, ProviderDescriptor> providerDescriptorMap;

    @Autowired
    public DescriptorMap(final List<ChannelDescriptor> channelDescriptors, final List<ProviderDescriptor> providerDescriptors) {
        descriptorMap = new HashMap<>(channelDescriptors.size() + providerDescriptors.size());
        channelDescriptorMap = initMap(channelDescriptors);
        providerDescriptorMap = initMap(providerDescriptors);
    }

    private <D extends Descriptor> Map<String, D> initMap(final List<D> descriptorList) {
        final Map<String, D> descriptorMapping = new HashMap<>(descriptorList.size());
        descriptorList.forEach(descriptor -> {
            descriptorMap.put(descriptor.getName(), descriptor);
            descriptorMapping.put(descriptor.getName(), descriptor);
        });
        return descriptorMapping;
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
