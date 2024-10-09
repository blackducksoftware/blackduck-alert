package com.blackduck.integration.alert.common.descriptor;

import java.util.Optional;
import java.util.Set;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;

public abstract class ComponentDescriptor extends Descriptor {
    protected ComponentDescriptor(DescriptorKey descriptorKey) {
        super(descriptorKey, DescriptorType.COMPONENT, Set.of(ConfigContextEnum.GLOBAL));
    }

    @Override
    public Optional<DistributionConfigurationValidator> getDistributionValidator() {
        return Optional.empty();
    }

}
