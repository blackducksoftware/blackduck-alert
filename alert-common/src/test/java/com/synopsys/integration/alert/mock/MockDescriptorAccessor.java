package com.synopsys.integration.alert.mock;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class MockDescriptorAccessor implements DescriptorAccessor {

    private final List<ConfigField> configFields;

    public MockDescriptorAccessor(List<ConfigField> configFields) {
        this.configFields = configFields;
    }

    public final List<DefinedFieldModel> createDefinedFields(ConfigContextEnum context) {
        return configFields.stream()
                   .map(configField -> new DefinedFieldModel(configField.getKey(), context, configField.isSensitive()))
                   .collect(Collectors.toList());
    }

    @Override
    public List<RegisteredDescriptorModel> getRegisteredDescriptors() throws AlertDatabaseConstraintException {
        return null;
    }

    @Override
    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorByKey(DescriptorKey descriptorKey) throws AlertDatabaseConstraintException {
        return Optional.empty();
    }

    @Override
    public List<RegisteredDescriptorModel> getRegisteredDescriptorsByType(DescriptorType descriptorType) throws AlertDatabaseConstraintException {
        return null;
    }

    @Override
    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorById(Long descriptorId) throws AlertDatabaseConstraintException {
        return Optional.empty();
    }

    @Override
    public List<DefinedFieldModel> getFieldsForDescriptor(DescriptorKey descriptorKey, ConfigContextEnum context) throws AlertDatabaseConstraintException {
        return createDefinedFields(context);
    }

    @Override
    public List<DefinedFieldModel> getFieldsForDescriptorById(Long descriptorId, ConfigContextEnum context) throws AlertDatabaseConstraintException {
        return createDefinedFields(context);
    }

}
