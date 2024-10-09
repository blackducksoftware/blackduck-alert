package com.blackduck.integration.alert.common.descriptor;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;
import com.blackduck.integration.alert.common.util.DataStructureUtils;

@Component
public class DescriptorMap {
    private final Map<String, DescriptorKey> universalKeyToFullKey;
    private final Map<DescriptorKey, Descriptor> keyToDescriptor;
    private final Map<DescriptorType, Set<Descriptor>> typeToDescriptor;

    @Autowired
    public DescriptorMap(List<DescriptorKey> descriptorKeys, List<Descriptor> descriptors) {
        this.universalKeyToFullKey = DataStructureUtils.mapToValues(descriptorKeys, DescriptorKey::getUniversalKey);
        this.keyToDescriptor = DataStructureUtils.mapToValues(descriptors, Descriptor::getDescriptorKey);
        this.typeToDescriptor = initDescriptorTypeMap(descriptors);
    }

    public Optional<DescriptorKey> getDescriptorKey(String key) {
        return Optional.ofNullable(universalKeyToFullKey.get(key));
    }

    public Optional<Descriptor> getDescriptor(DescriptorKey key) {
        return Optional.ofNullable(keyToDescriptor.get(key));
    }

    public Set<DescriptorKey> getDescriptorKeys() {
        return keyToDescriptor.keySet();
    }

    public Set<Descriptor> getDescriptorByType(DescriptorType descriptorType) {
        return typeToDescriptor.get(descriptorType);
    }

    public Map<DescriptorKey, Descriptor> getDescriptorMap() {
        return keyToDescriptor;
    }

    private static EnumMap<DescriptorType, Set<Descriptor>> initDescriptorTypeMap(List<Descriptor> descriptors) {
        EnumMap<DescriptorType, Set<Descriptor>> typeToDescriptors = new EnumMap<>(DescriptorType.class);
        for (Descriptor descriptor : descriptors) {
            typeToDescriptors.computeIfAbsent(descriptor.getType(), ignored -> new HashSet<>()).add(descriptor);
        }
        return typeToDescriptors;
    }

}
