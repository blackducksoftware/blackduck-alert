/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class DescriptorMap {
    private final Map<String, DescriptorKey> descriptorKeys;
    private final Map<DescriptorKey, Descriptor> descriptorMapping;
    private final Map<DescriptorKey, ChannelDescriptor> channelDescriptorMapping;
    private final Map<DescriptorKey, ProviderDescriptor> providerDescriptorMapping;
    private final Map<DescriptorKey, ComponentDescriptor> componentDescriptorMapping;

    @Autowired
    public DescriptorMap(List<DescriptorKey> descriptorKeys, List<ChannelDescriptor> channelDescriptors, List<ProviderDescriptor> providerDescriptors, List<ComponentDescriptor> componentDescriptors) throws AlertException {
        this.descriptorKeys = DataStructureUtils.mapToValues(descriptorKeys, DescriptorKey::getUniversalKey);
        descriptorMapping = new HashMap<>();
        channelDescriptorMapping = initDescriptorMap(channelDescriptors);
        providerDescriptorMapping = initDescriptorMap(providerDescriptors);
        componentDescriptorMapping = initDescriptorMap(componentDescriptors);
    }

    public Optional<DescriptorKey> getDescriptorKey(String key) {
        return Optional.ofNullable(descriptorKeys.get(key));
    }

    public Optional<Descriptor> getDescriptor(DescriptorKey key) {
        return Optional.ofNullable(descriptorMapping.get(key));
    }

    public Set<Descriptor> getDescriptorByType(DescriptorType descriptorType) {
        return descriptorMapping.entrySet().stream()
                   .map(Map.Entry::getValue)
                   .filter(descriptor -> descriptor.getType() == descriptorType)
                   .collect(Collectors.toSet());
    }

    public Optional<ChannelDescriptor> getChannelDescriptor(DescriptorKey key) {
        return Optional.ofNullable(channelDescriptorMapping.get(key));
    }

    public Optional<ProviderDescriptor> getProviderDescriptor(DescriptorKey key) {
        return Optional.ofNullable(providerDescriptorMapping.get(key));
    }

    public Optional<ComponentDescriptor> getComponentDescriptor(DescriptorKey key) {
        return Optional.ofNullable(componentDescriptorMapping.get(key));
    }

    public Map<DescriptorKey, Descriptor> getDescriptorMap() {
        return descriptorMapping;
    }

    public Map<DescriptorKey, ChannelDescriptor> getChannelDescriptorMap() {
        return channelDescriptorMapping;
    }

    public Map<DescriptorKey, ProviderDescriptor> getProviderDescriptorMap() {
        return providerDescriptorMapping;
    }

    public Map<DescriptorKey, ComponentDescriptor> getComponentDescriptorMap() {
        return componentDescriptorMapping;
    }

    private <D extends Descriptor> Map<DescriptorKey, D> initDescriptorMap(List<D> descriptorList) throws AlertException {
        Map<DescriptorKey, D> specificDescriptorMapping = new HashMap<>(descriptorList.size());
        for (D descriptor : descriptorList) {
            DescriptorKey descriptorKey = descriptor.getDescriptorKey();
            if (descriptorMapping.containsKey(descriptorKey)) {
                throw new AlertException("Found duplicate descriptor name of: " + descriptorKey.getUniversalKey());
            }
            descriptorMapping.put(descriptorKey, descriptor);
            specificDescriptorMapping.put(descriptorKey, descriptor);
        }
        return specificDescriptorMapping;
    }

}
