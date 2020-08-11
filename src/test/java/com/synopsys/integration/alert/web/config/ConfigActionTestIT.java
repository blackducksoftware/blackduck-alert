package com.synopsys.integration.alert.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.List;
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
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
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
        FieldModelProcessor spiedFieldModelProcessor = Mockito.spy(fieldModelProcessor);
        Mockito.doReturn(List.of()).when(spiedFieldModelProcessor).validateFieldModel(Mockito.any());
        ConfigActions configActions = new ConfigActions(configurationAccessor, spiedFieldModelProcessor, descriptorProcessor, configurationFieldModelConverter);
        ConfigurationFieldModel proxyHost = ConfigurationFieldModel.create(ProxyManager.KEY_PROXY_HOST);
        proxyHost.setFieldValue("proxyHost");
        ConfigurationFieldModel proxyPort = ConfigurationFieldModel.create(ProxyManager.KEY_PROXY_PORT);
        proxyPort.setFieldValue("80");
        ConfigurationFieldModel proxyUsername = ConfigurationFieldModel.create(ProxyManager.KEY_PROXY_USERNAME);
        proxyUsername.setFieldValue("username");
        ConfigurationFieldModel proxyPassword = ConfigurationFieldModel.createSensitive(ProxyManager.KEY_PROXY_PWD);
        proxyPassword.setFieldValue("somestuff");
        ConfigurationModel configurationModel = configurationAccessor.createConfiguration(settingsDescriptorKey, ConfigContextEnum.GLOBAL, Set.of(proxyHost, proxyPort, proxyUsername, proxyPassword));

        FieldValueModel proxyHostFieldValue = new FieldValueModel(Set.of("proxyHost"), true);
        FieldValueModel proxyPortFieldValue = new FieldValueModel(Set.of("80"), true);
        final String newUsername = "Hello";
        FieldValueModel proxyUsernameFieldValue = new FieldValueModel(Set.of(newUsername), true);
        FieldValueModel proxyPasswordFieldValue = new FieldValueModel(Set.of(), false);

        Long longConfigId = configurationModel.getConfigurationId();
        String configId = String.valueOf(longConfigId);

        FieldModel fieldModel = new FieldModel(configId, settingsDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(),
            new HashMap<>(Map.of(ProxyManager.KEY_PROXY_HOST, proxyHostFieldValue, ProxyManager.KEY_PROXY_PORT, proxyPortFieldValue,
                ProxyManager.KEY_PROXY_USERNAME, proxyUsernameFieldValue, ProxyManager.KEY_PROXY_PWD, proxyPasswordFieldValue)));
        FieldModel updatedConfig = configActions.updateConfig(longConfigId, fieldModel);

        Map<String, FieldValueModel> updatedValues = updatedConfig.getKeyToValues();

        assertEquals(newUsername, updatedValues.get(ProxyManager.KEY_PROXY_USERNAME).getValue().orElse(""));
        assertNull(updatedValues.get(ProxyManager.KEY_PROXY_PWD), "Saving an empty values should remove it from DB.");
    }
}
