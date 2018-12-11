package com.synopsys.integration.alert.common.database;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DescriptorAccessor.RegisteredDescriptorModel;

public interface BaseDescriptorAccessor {

    List<RegisteredDescriptorModel> getRegisteredDescriptors();

    Optional<RegisteredDescriptorModel> getRegisteredDescriptorByName(final String descriptorName) throws AlertDatabaseConstraintException;

    Optional<RegisteredDescriptorModel> getRegisteredDescriptorById(final Long descriptorId) throws AlertDatabaseConstraintException;

    boolean registerDescriptorWithoutFields(final String descriptorName) throws AlertDatabaseConstraintException;

    boolean registerDescriptor(final String descriptorName, final Collection<DefinedFieldModel> descriptorFields) throws AlertDatabaseConstraintException;

    boolean unregisterDescriptor(final String descriptorName) throws AlertDatabaseConstraintException;

    List<DefinedFieldModel> getFieldsForDescriptor(final String descriptorName, final ConfigContextEnum context) throws AlertDatabaseConstraintException;

    List<DefinedFieldModel> getFieldsForDescriptorById(final Long descriptorId, final ConfigContextEnum context) throws AlertDatabaseConstraintException;

    DefinedFieldModel addDescriptorField(final Long descriptorId, final DefinedFieldModel descriptorField) throws AlertDatabaseConstraintException;

    DefinedFieldModel updateFieldKey(final String oldKey, final String newKey) throws AlertDatabaseConstraintException;

    boolean deleteDescriptorField(final Long descriptorId, final DefinedFieldModel descriptorField) throws AlertDatabaseConstraintException;

    boolean deleteDescriptorField(final Long descriptorId, final String fieldKey) throws AlertDatabaseConstraintException;
}
