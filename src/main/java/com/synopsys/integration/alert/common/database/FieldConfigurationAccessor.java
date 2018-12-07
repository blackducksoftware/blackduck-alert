package com.synopsys.integration.alert.common.database;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;

public interface FieldConfigurationAccessor {

    List<ConfigurationModel> getConfigurationByContext(ConfigContextEnum context) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationById(Long id) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationsByDescriptorName(String descriptorName) throws AlertDatabaseConstraintException;

    Optional<ConfigurationModel> getConfigurationByDescriptorNameAndContext(String descriptorName, ConfigContextEnum context);

    ConfigurationModel createEmptyConfiguration(String descriptorName, ConfigContextEnum context) throws AlertDatabaseConstraintException;

    ConfigurationModel createConfiguration(String descriptorName, ConfigContextEnum context, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    ConfigurationModel updateConfiguration(final Long descriptorConfigId, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    void deleteConfiguration(final ConfigurationModel configModel) throws AlertDatabaseConstraintException;

    void deleteConfiguration(final Long descriptorConfigId) throws AlertDatabaseConstraintException;

}
