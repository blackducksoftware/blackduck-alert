package com.blackduck.integration.alert.mock;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;
import com.blackduck.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.blackduck.integration.alert.common.persistence.model.DefinedFieldModel;
import com.blackduck.integration.alert.common.persistence.model.RegisteredDescriptorModel;

public class MockDescriptorAccessor implements DescriptorAccessor {
    private final List<DefinedFieldModel> definedFieldModels;

    public MockDescriptorAccessor(List<DefinedFieldModel> definedFieldModels) {
        this.definedFieldModels = definedFieldModels;
    }

    @Override
    public List<RegisteredDescriptorModel> getRegisteredDescriptors() {
        return null;
    }

    @Override
    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorByKey(DescriptorKey descriptorKey) {
        return Optional.empty();
    }

    @Override
    public List<RegisteredDescriptorModel> getRegisteredDescriptorsByType(DescriptorType descriptorType) {
        return null;
    }

    @Override
    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorById(Long descriptorId) {
        return Optional.empty();
    }

    @Override
    public List<DefinedFieldModel> getFieldsForDescriptor(DescriptorKey descriptorKey, ConfigContextEnum context) {
        return definedFieldModels
            .stream()
            .filter(field -> field.getContexts().contains(context))
            .collect(Collectors.toList());
    }

    @Override
    public List<DefinedFieldModel> getFieldsForDescriptorById(Long descriptorId, ConfigContextEnum context) {
        return getFieldsForDescriptor(null, context);
    }

}
