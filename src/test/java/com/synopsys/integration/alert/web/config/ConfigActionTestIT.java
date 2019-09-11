package com.synopsys.integration.alert.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class ConfigActionTestIT extends AlertIntegrationTest {
    @Autowired
    private ConfigurationAccessor configurationAccessor;
    @Autowired
    private FieldModelProcessor fieldModelProcessor;
    @Autowired
    private DescriptorProcessor descriptorProcessor;
    @Autowired
    private ConfigurationFieldModelConverter configurationFieldModelConverter;
    @Autowired
    private SettingsDescriptorKey settingsDescriptorKey;

    @Test
    public void deleteSensitiveFieldFromConfig() throws AlertException, AlertFieldException {
        final FieldModelProcessor spiedFieldModelProcessor = Mockito.spy(fieldModelProcessor);
        Mockito.doReturn(Map.of()).when(spiedFieldModelProcessor).validateFieldModel(Mockito.any());
        final ConfigActions configActions = new ConfigActions(configurationAccessor, spiedFieldModelProcessor, descriptorProcessor, configurationFieldModelConverter);
        final ConfigurationFieldModel proxyHost = ConfigurationFieldModel.create(SettingsDescriptor.KEY_PROXY_HOST);
        proxyHost.setFieldValue("proxyHost");
        final ConfigurationFieldModel proxyPort = ConfigurationFieldModel.create(SettingsDescriptor.KEY_PROXY_PORT);
        proxyPort.setFieldValue("80");
        final ConfigurationFieldModel proxyUsername = ConfigurationFieldModel.create(SettingsDescriptor.KEY_PROXY_USERNAME);
        proxyUsername.setFieldValue("username");
        final ConfigurationFieldModel proxyPassword = ConfigurationFieldModel.createSensitive(SettingsDescriptor.KEY_PROXY_PWD);
        proxyPassword.setFieldValue("somestuff");
        final ConfigurationModel configurationModel = configurationAccessor.createConfiguration(settingsDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL, Set.of(proxyHost, proxyPort, proxyUsername, proxyPassword));

        final FieldValueModel proxyHostFieldValue = new FieldValueModel(Set.of("proxyHost"), true);
        final FieldValueModel proxyPortFieldValue = new FieldValueModel(Set.of("80"), true);
        final String newUsername = "Hello";
        final FieldValueModel proxyUsernameFieldValue = new FieldValueModel(Set.of(newUsername), true);
        final FieldValueModel proxyPasswordFieldValue = new FieldValueModel(Set.of(), false);

        final Long longConfigId = configurationModel.getConfigurationId();
        final String configId = String.valueOf(longConfigId);

        final FieldModel fieldModel = new FieldModel(configId, settingsDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(),
            new HashMap<>(Map.of(SettingsDescriptor.KEY_PROXY_HOST, proxyHostFieldValue, SettingsDescriptor.KEY_PROXY_PORT, proxyPortFieldValue,
                SettingsDescriptor.KEY_PROXY_USERNAME, proxyUsernameFieldValue, SettingsDescriptor.KEY_PROXY_PWD, proxyPasswordFieldValue)));
        final FieldModel updatedConfig = configActions.updateConfig(longConfigId, fieldModel);

        final Map<String, FieldValueModel> updatedValues = updatedConfig.getKeyToValues();

        assertEquals(newUsername, updatedValues.get(SettingsDescriptor.KEY_PROXY_USERNAME).getValue().orElse(""));
        assertNull(updatedValues.get(SettingsDescriptor.KEY_PROXY_PWD), "Saving an empty values should remove it from DB.");
    }
}
