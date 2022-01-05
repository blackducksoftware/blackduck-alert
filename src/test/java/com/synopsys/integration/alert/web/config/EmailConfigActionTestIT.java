package com.synopsys.integration.alert.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.email.action.EmailGlobalCrudActions;
import com.synopsys.integration.alert.channel.email.convert.EmailGlobalConfigurationModelConverter;
import com.synopsys.integration.alert.channel.email.convert.EmailGlobalConfigurationModelSaveActions;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteSaveActions;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.FieldModelProcessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.certificates.web.PKIXErrorResponseFactory;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.api.config.ConfigActions;
import com.synopsys.integration.alert.web.api.config.GlobalConfigurationModelToConcreteConversionService;

@AlertIntegrationTest
public class EmailConfigActionTestIT {
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
    private EmailGlobalConfigAccessor emailGlobalConfigAccessor;

    @BeforeEach
    void deleteDefaultConfig() {
        emailGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .map(EmailGlobalConfigModel::getId)
            .map(id -> UUID.fromString(id))
            .ifPresent(emailGlobalConfigAccessor::deleteConfiguration);
    }

    @Test
    void createEmailGlobalConfigTest() {
        AuthorizationManager authorizationManager = createEmailAuthorizationManager();
        EmailGlobalCrudActions emailGlobalCrudActions = createEmailCrudActions(authorizationManager);
        GlobalConfigurationModelToConcreteConversionService globalConfigurationModelToConcreteConversionService = createConversionService(emailGlobalCrudActions);
        ConfigActions configActions = new ConfigActions(authorizationManager, descriptorAccessor, configurationModelConfigurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, descriptorMap,
            pkixErrorResponseFactory, encryptionUtility, settingsDescriptorKey, globalConfigurationModelToConcreteConversionService);
        FieldModel fieldModel = createEmailFieldModel();
        configActions.create(fieldModel);

        Optional<EmailGlobalConfigModel> staticEmailConfig = emailGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(staticEmailConfig.isPresent());
        EmailGlobalConfigModel staticModel = staticEmailConfig.get();
        assertEquals(Boolean.TRUE, staticModel.getSmtpAuth().orElse(null));
        assertEquals(TEST_AUTH_USER, staticModel.getSmtpUsername().orElse(null));
        assertEquals(TEST_AUTH_PASSWORD, staticModel.getSmtpPassword().orElse(null));
        assertEquals(TEST_SMTP_HOST, staticModel.getSmtpHost().orElse(null));
        assertEquals(Integer.valueOf(TEST_SMTP_PORT), staticModel.getSmtpPort().orElse(null));
        assertEquals(TEST_FROM, staticModel.getSmtpFrom().orElse(null));

        String propertyValue = staticModel.getAdditionalJavaMailProperties()
            .map(map -> map.get(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey()))
            .orElse(null);
        assertEquals("true", propertyValue);
    }

    @Test
    void updatePasswordEmailGlobalConfigTest() throws AlertConfigurationException {
        AuthorizationManager authorizationManager = createEmailAuthorizationManager();
        EmailGlobalCrudActions emailGlobalCrudActions = createEmailCrudActions(authorizationManager);
        GlobalConfigurationModelToConcreteConversionService globalConfigurationModelToConcreteConversionService = createConversionService(emailGlobalCrudActions);
        ConfigActions configActions = new ConfigActions(authorizationManager, descriptorAccessor, configurationModelConfigurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, descriptorMap,
            pkixErrorResponseFactory, encryptionUtility, settingsDescriptorKey, globalConfigurationModelToConcreteConversionService);
        FieldModel fieldModel = createEmailFieldModel();
        fieldModel = configActions.create(fieldModel).getContent().orElseThrow(() -> new AlertConfigurationException("Couldn't create configuration"));

        String updatedHost = "updated." + TEST_SMTP_HOST;
        String updatedPassword = "updatedPassword";
        fieldModel.putField(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), new FieldValueModel(List.of(updatedHost), false));
        fieldModel.putField(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), new FieldValueModel(List.of(updatedPassword), false));

        configActions.update(Long.valueOf(fieldModel.getId()), fieldModel);

        Optional<EmailGlobalConfigModel> staticEmailConfig = emailGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(staticEmailConfig.isPresent());
        EmailGlobalConfigModel staticModel = staticEmailConfig.get();
        assertEquals(Boolean.TRUE, staticModel.getSmtpAuth().orElse(null));
        assertEquals(TEST_AUTH_USER, staticModel.getSmtpUsername().orElse(null));
        assertEquals(updatedPassword, staticModel.getSmtpPassword().orElse(null));
        assertEquals(updatedHost, staticModel.getSmtpHost().orElse(null));
        assertEquals(Integer.valueOf(TEST_SMTP_PORT), staticModel.getSmtpPort().orElse(null));
        assertEquals(TEST_FROM, staticModel.getSmtpFrom().orElse(null));

        String propertyValue = staticModel.getAdditionalJavaMailProperties()
            .map(map -> map.get(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey()))
            .orElse(null);
        assertEquals("true", propertyValue);
    }

    @Test
    void updateReadExistingPasswordEmailGlobalConfigTest() throws AlertConfigurationException {
        AuthorizationManager authorizationManager = createEmailAuthorizationManager();
        EmailGlobalCrudActions emailGlobalCrudActions = createEmailCrudActions(authorizationManager);
        GlobalConfigurationModelToConcreteConversionService globalConfigurationModelToConcreteConversionService = createConversionService(emailGlobalCrudActions);
        ConfigActions configActions = new ConfigActions(authorizationManager, descriptorAccessor, configurationModelConfigurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, descriptorMap,
            pkixErrorResponseFactory, encryptionUtility, settingsDescriptorKey, globalConfigurationModelToConcreteConversionService);
        FieldModel fieldModel = createEmailFieldModel();
        fieldModel = configActions.create(fieldModel).getContent().orElseThrow(() -> new AlertConfigurationException("Couldn't create configuration"));
        String updatedHost = "updated." + TEST_SMTP_HOST;
        fieldModel.putField(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), new FieldValueModel(List.of(updatedHost), false));

        configActions.update(Long.valueOf(fieldModel.getId()), fieldModel);

        Optional<EmailGlobalConfigModel> staticEmailConfig = emailGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(staticEmailConfig.isPresent());
        EmailGlobalConfigModel staticModel = staticEmailConfig.get();
        assertEquals(Boolean.TRUE, staticModel.getSmtpAuth().orElse(null));
        assertEquals(TEST_AUTH_USER, staticModel.getSmtpUsername().orElse(null));
        assertEquals(TEST_AUTH_PASSWORD, staticModel.getSmtpPassword().orElse(null));
        assertEquals(updatedHost, staticModel.getSmtpHost().orElse(null));
        assertEquals(Integer.valueOf(TEST_SMTP_PORT), staticModel.getSmtpPort().orElse(null));
        assertEquals(TEST_FROM, staticModel.getSmtpFrom().orElse(null));

        String propertyValue = staticModel.getAdditionalJavaMailProperties()
            .map(map -> map.get(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey()))
            .orElse(null);
        assertEquals("true", propertyValue);
    }

    @Test
    void deleteEmailGlobalConfigTest() throws AlertConfigurationException {
        AuthorizationManager authorizationManager = createEmailAuthorizationManager();
        EmailGlobalCrudActions emailGlobalCrudActions = createEmailCrudActions(authorizationManager);
        GlobalConfigurationModelToConcreteConversionService globalConfigurationModelToConcreteConversionService = createConversionService(emailGlobalCrudActions);
        ConfigActions configActions = new ConfigActions(authorizationManager, descriptorAccessor, configurationModelConfigurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, descriptorMap,
            pkixErrorResponseFactory, encryptionUtility, settingsDescriptorKey, globalConfigurationModelToConcreteConversionService);
        FieldModel fieldModel = createEmailFieldModel();
        fieldModel = configActions.create(fieldModel).getContent().orElseThrow(() -> new AlertConfigurationException("Couldn't create configuration"));

        configActions.delete(Long.valueOf(fieldModel.getId()));
        Optional<EmailGlobalConfigModel> staticEmailConfig = emailGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(staticEmailConfig.isEmpty());

    }

    private AuthorizationManager createEmailAuthorizationManager() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }

    private EmailGlobalCrudActions createEmailCrudActions(AuthorizationManager authorizationManager) {
        return new EmailGlobalCrudActions(authorizationManager, emailGlobalConfigAccessor, new EmailGlobalConfigurationValidator());
    }

    private GlobalConfigurationModelToConcreteConversionService createConversionService(EmailGlobalCrudActions emailGlobalCrudActions) {
        EmailGlobalConfigurationModelConverter modelConverter = new EmailGlobalConfigurationModelConverter();
        EmailGlobalConfigurationModelSaveActions emailGlobalConfigurationModelSaveActions = new EmailGlobalConfigurationModelSaveActions(modelConverter, emailGlobalCrudActions, emailGlobalConfigAccessor);
        List<GlobalConfigurationModelToConcreteSaveActions> conversionActions = List.of(emailGlobalConfigurationModelSaveActions);
        return new GlobalConfigurationModelToConcreteConversionService(conversionActions, descriptorMap);
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
