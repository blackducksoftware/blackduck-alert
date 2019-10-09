package com.synopsys.integration.alert.common.descriptor.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;

public interface SettingsUtility {

    String getSettingsName();

    Optional<ConfigurationModel> getSettings() throws AlertException;

    ConfigurationModel saveSettings(Collection<ConfigurationFieldModel> fieldModels) throws AlertException;

    ConfigurationModel updateSettings(Long id, Collection<ConfigurationFieldModel> fieldModels) throws AlertException;

    List<DefinedFieldModel> getSettingsFields() throws AlertException;
}
