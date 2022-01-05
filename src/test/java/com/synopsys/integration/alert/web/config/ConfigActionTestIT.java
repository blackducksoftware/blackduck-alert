package com.synopsys.integration.alert.web.config;

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
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.FieldModelProcessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.certificates.web.PKIXErrorResponseFactory;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.api.config.ConfigActions;
import com.synopsys.integration.alert.web.api.config.GlobalConfigurationModelToConcreteConversionService;

import junit.framework.AssertionFailedError;

@AlertIntegrationTest
class ConfigActionTestIT {

    @Autowired
    private ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
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
    @Autowired
    private EncryptionUtility encryptionUtility;

    @Test
    void deleteSensitiveFieldFromConfig() {
        GlobalConfigurationModelToConcreteConversionService conversionService = new GlobalConfigurationModelToConcreteConversionService(List.of(), descriptorMap);
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        ConfigActions configActions = new ConfigActions(authorizationManager, descriptorAccessor, configurationModelConfigurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, descriptorMap,
            pkixErrorResponseFactory, encryptionUtility, settingsDescriptorKey, conversionService);
        ConfigurationFieldModel proxyHost = ConfigurationFieldModel.create(ProxyManager.KEY_PROXY_HOST);
        proxyHost.setFieldValue("proxyHost");
        ConfigurationFieldModel proxyPort = ConfigurationFieldModel.create(ProxyManager.KEY_PROXY_PORT);
        proxyPort.setFieldValue("80");
        ConfigurationFieldModel proxyUsername = ConfigurationFieldModel.create(ProxyManager.KEY_PROXY_USERNAME);
        proxyUsername.setFieldValue("username");
        ConfigurationFieldModel proxyPassword = ConfigurationFieldModel.createSensitive(ProxyManager.KEY_PROXY_PWD);
        proxyPassword.setFieldValue("somestuff");
        ConfigurationFieldModel encryptionPassword = ConfigurationFieldModel.createSensitive(SettingsDescriptor.KEY_ENCRYPTION_PWD);
        encryptionPassword.setFieldValue("pants");
        ConfigurationFieldModel encryptionSalt = ConfigurationFieldModel.createSensitive(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);
        encryptionSalt.setFieldValue("salty pants");
        ConfigurationModel configurationModel = configurationModelConfigurationAccessor.createConfiguration(settingsDescriptorKey, ConfigContextEnum.GLOBAL, Set.of(proxyHost, proxyPort, proxyUsername, proxyPassword, encryptionPassword, encryptionSalt));

        FieldValueModel proxyHostFieldValue = new FieldValueModel(Set.of("proxyHost"), true);
        FieldValueModel proxyPortFieldValue = new FieldValueModel(Set.of("80"), true);
        FieldValueModel proxyUsernameFieldValue = new FieldValueModel(Set.of(), false);
        FieldValueModel proxyPasswordFieldValue = new FieldValueModel(Set.of(), false);
        FieldValueModel encryptionPasswordFieldValue = new FieldValueModel(Set.of("encryptionPassword"), true);
        FieldValueModel encryptionSaltFieldValue = new FieldValueModel(Set.of("sodiumChloride"), true);

        Long longConfigId = configurationModel.getConfigurationId();
        String configId = String.valueOf(longConfigId);

        FieldModel fieldModel = new FieldModel(configId, settingsDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(),
            new HashMap<>(Map.of(ProxyManager.KEY_PROXY_HOST, proxyHostFieldValue, ProxyManager.KEY_PROXY_PORT, proxyPortFieldValue,
                ProxyManager.KEY_PROXY_USERNAME, proxyUsernameFieldValue, ProxyManager.KEY_PROXY_PWD, proxyPasswordFieldValue,
                SettingsDescriptor.KEY_ENCRYPTION_PWD, encryptionPasswordFieldValue, SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, encryptionSaltFieldValue)));

        ActionResponse<FieldModel> response = configActions.update(longConfigId, fieldModel);
        assertTrue(response.hasContent());
        FieldModel updatedConfig = response.getContent().orElseThrow(() -> new AssertionFailedError("content missing from response."));

        assertTrue(updatedConfig.getFieldValue(ProxyManager.KEY_PROXY_USERNAME).isEmpty(), "Need to remove username in order to remove password as well.");
        assertTrue(updatedConfig.getFieldValue(ProxyManager.KEY_PROXY_PWD).isEmpty(), "Saving an empty values should remove it from DB.");
    }

}

