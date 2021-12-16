package com.synopsys.integration.alert.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
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
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.api.config.ConfigActions;
import com.synopsys.integration.alert.web.api.config.GlobalFieldModelToConcreteConversionService;

import junit.framework.AssertionFailedError;

@Transactional
@AlertIntegrationTest
class ConfigActionTestIT {

    public static final String TEST_AUTH_REQUIRED = "true";
    public static final String TEST_FROM = "test.user@some.company.example.com";
    public static final String TEST_SMTP_HOST = "smtp.server.example.com";
    public static final String TEST_AUTH_PASSWORD = "apassword";
    public static final String TEST_SMTP_PORT = "2025";
    public static final String TEST_AUTH_USER = "auser";

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
    @Autowired
    private GlobalFieldModelToConcreteConversionService globalFieldModelToConcreteConversionService;
    @Autowired
    private EmailGlobalConfigAccessor emailGlobalConfigAccessor;

    @Test
    void deleteSensitiveFieldFromConfig() {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        ConfigActions configActions = new ConfigActions(authorizationManager, descriptorAccessor, configurationModelConfigurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, descriptorMap,
            pkixErrorResponseFactory, encryptionUtility, settingsDescriptorKey, globalFieldModelToConcreteConversionService);
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

    @Test
    @Disabled
    void createEmailGlobalConfigTest() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigActions configActions = new ConfigActions(authorizationManager, descriptorAccessor, configurationModelConfigurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, descriptorMap,
            pkixErrorResponseFactory, encryptionUtility, settingsDescriptorKey, globalFieldModelToConcreteConversionService);
        FieldModel fieldModel = createEmailFieldModel();
        configActions.create(fieldModel);

        Optional<EmailGlobalConfigModel> staticEmailConfig = emailGlobalConfigAccessor.getConfigurationByName(ConfigurationAccessor.DEFAULT_CONFIGURATION_NAME);
        assertTrue(staticEmailConfig.isPresent());
        EmailGlobalConfigModel staticModel = staticEmailConfig.get();
        assertEquals(Boolean.TRUE, staticModel.getSmtpAuth().orElse(null));
        assertEquals(TEST_AUTH_USER, staticModel.getSmtpUsername().orElse(null));
        assertEquals(TEST_AUTH_PASSWORD, encryptionUtility.decrypt(staticModel.getSmtpPassword().orElse(null)));
        assertEquals(TEST_SMTP_HOST, staticModel.getSmtpHost().orElse(null));
        assertEquals(Integer.valueOf(TEST_SMTP_PORT), staticModel.getSmtpPort().orElse(null));
        assertEquals(TEST_FROM, staticModel.getSmtpFrom().orElse(null));

        String propertyValue = staticModel.getAdditionalJavaMailProperties()
            .map(map -> map.get(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey()))
            .orElse(null);
        assertEquals("true", propertyValue);
    }

    private FieldModel createEmailFieldModel() {
        Map<String, FieldValueModel> fieldValuesMap = new HashMap<>();
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), new FieldValueModel(List.of(TEST_FROM), false));
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), new FieldValueModel(List.of(TEST_SMTP_HOST), false));
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), new FieldValueModel(List.of(TEST_SMTP_PORT), false));
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), new FieldValueModel(List.of(TEST_AUTH_REQUIRED), false));
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), new FieldValueModel(List.of(TEST_AUTH_PASSWORD), false));
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), new FieldValueModel(List.of(TEST_AUTH_USER), false));

        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), new FieldValueModel(List.of("true"), false));
        return new FieldModel(ChannelKeys.EMAIL.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), fieldValuesMap);
    }
}

