package com.synopsys.integration.alert.component.settings;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;

@Component
public class DefaultSettingsUtility implements SettingsUtility {
    private SettingsDescriptorKey settingsDescriptorKey;
    private ConfigurationAccessor configurationAccessor;

    @Autowired
    public DefaultSettingsUtility(SettingsDescriptorKey settingsDescriptorKey, ConfigurationAccessor configurationAccessor) {
        this.settingsDescriptorKey = settingsDescriptorKey;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public Optional<ConfigurationModel> getSettings() throws AlertException {
        return configurationAccessor.getConfigurationByDescriptorNameAndContext(settingsDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL).stream().findFirst();
    }

    @Override
    public ConfigurationModel saveSettings(final Collection<ConfigurationFieldModel> fieldModels) throws AlertException {
        return configurationAccessor.createConfiguration(settingsDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL, fieldModels);
    }

    @Override
    public ConfigurationModel updateSettings(final Long id, final Collection<ConfigurationFieldModel> fieldModels) throws AlertException {
        return configurationAccessor.updateConfiguration(id, fieldModels);
    }
}
