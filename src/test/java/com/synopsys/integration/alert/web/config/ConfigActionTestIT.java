package com.synopsys.integration.alert.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.FieldModelProcessor;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.certificates.web.PKIXErrorResponseFactory;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.api.config.ConfigActions;

import junit.framework.AssertionFailedError;

@AlertIntegrationTest
public class ConfigActionTestIT {
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
    @Autowired
    private PKIXErrorResponseFactory pkixErrorResponseFactory;
    @Autowired
    private DescriptorMap descriptorMap;
    @Autowired
    private DescriptorAccessor descriptorAccessor;

    @Test
    public void deleteSensitiveFieldFromConfig() {
        FieldModelProcessor spiedFieldModelProcessor = Mockito.spy(fieldModelProcessor);
        Mockito.doReturn(List.of()).when(spiedFieldModelProcessor).validateFieldModel(Mockito.any());
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        ConfigActions configActions = new ConfigActions(authorizationManager, descriptorAccessor, configurationAccessor, spiedFieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, descriptorMap,
            pkixErrorResponseFactory);
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

        ActionResponse<FieldModel> response = configActions.update(longConfigId, fieldModel);
        assertTrue(response.hasContent());
        FieldModel updatedConfig = response.getContent().orElseThrow(() -> new AssertionFailedError("content missing from response."));
        Map<String, FieldValueModel> updatedValues = updatedConfig.getKeyToValues();

        assertEquals(newUsername, updatedValues.get(ProxyManager.KEY_PROXY_USERNAME).getValue().orElse(""));
        assertNull(updatedValues.get(ProxyManager.KEY_PROXY_PWD), "Saving an empty values should remove it from DB.");
    }

}
