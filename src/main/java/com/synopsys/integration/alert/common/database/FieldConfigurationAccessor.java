package com.synopsys.integration.alert.common.database;

import java.util.Collection;
import java.util.List;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;

public interface FieldConfigurationAccessor {

    List<ConfigurationModel> getConfigurationByContext(ConfigContextEnum context) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationById(Long id) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationsByDescriptorName(String descriptorName) throws AlertDatabaseConstraintException;

    List<ConfigurationModel> getConfigurationByDescriptorNameAndContext(String descriptorName, ConfigContextEnum context) throws AlertDatabaseConstraintException;

    ConfigurationModel createEmptyConfiguration(String descriptorName, ConfigContextEnum context) throws AlertDatabaseConstraintException;

    ConfigurationModel createConfiguration(String descriptorName, ConfigContextEnum context, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    ConfigurationModel updateConfiguration(final Long descriptorConfigId, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    void deleteConfiguration(final ConfigurationModel configModel) throws AlertDatabaseConstraintException;

    void deleteConfiguration(final Long descriptorConfigId) throws AlertDatabaseConstraintException;

    // TODO find a place for a method to pass a map of Strings to be immiediately converted to a new config

}
