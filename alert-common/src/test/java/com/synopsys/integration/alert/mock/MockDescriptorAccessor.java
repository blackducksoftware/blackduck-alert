package com.synopsys.integration.alert.mock;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.BaseDescriptorAccessor;

public class MockDescriptorAccessor implements BaseDescriptorAccessor {

    private final List<ConfigField> configFields;

    public MockDescriptorAccessor(final List<ConfigField> configFields) {
        this.configFields = configFields;
    }

    public final List<DefinedFieldModel> createDefinedFields(final ConfigContextEnum context) {
        return configFields.stream()
                   .map(configField -> new DefinedFieldModel(configField.getKey(), context, configField.isSensitive()))
                   .collect(Collectors.toList());
    }

    @Override
    public List<RegisteredDescriptorModel> getRegisteredDescriptors() throws AlertDatabaseConstraintException {
        return null;
    }

    @Override
    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorByName(final String descriptorName) throws AlertDatabaseConstraintException {
        return Optional.empty();
    }

    @Override
    public List<RegisteredDescriptorModel> getRegisteredDescriptorsByType(final DescriptorType descriptorType) throws AlertDatabaseConstraintException {
        return null;
    }

    @Override
    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorById(final Long descriptorId) throws AlertDatabaseConstraintException {
        return Optional.empty();
    }

    @Override
    public List<DefinedFieldModel> getFieldsForDescriptor(final String descriptorName, final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        return createDefinedFields(context);
    }

    @Override
    public List<DefinedFieldModel> getFieldsForDescriptorById(final Long descriptorId, final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        return createDefinedFields(context);
    }
}
