package com.synopsys.integration.alert.component.settings;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;

@Component
public class DefaultSettingsUtility implements SettingsUtility {
    private SettingsDescriptorKey settingsDescriptorKey;
    private ConfigurationAccessor configurationAccessor;
    private DescriptorAccessor descriptorAccessor;

    @Autowired
    public DefaultSettingsUtility(SettingsDescriptorKey settingsDescriptorKey, ConfigurationAccessor configurationAccessor, final DescriptorAccessor descriptorAccessor) {
        this.settingsDescriptorKey = settingsDescriptorKey;
        this.configurationAccessor = configurationAccessor;
        this.descriptorAccessor = descriptorAccessor;
    }

    @Override
    public String getSettingsName() {
        return settingsDescriptorKey.getUniversalKey();
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

    @Override
    public List<DefinedFieldModel> getSettingsFields() throws AlertException {
        return descriptorAccessor.getFieldsForDescriptor(settingsDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL);
    }
}
