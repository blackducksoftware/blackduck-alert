package com.synopsys.integration.alert.channel.email.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.action.EmailGlobalCrudActions;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.database.email.EmailConfigurationEntity;
import com.synopsys.integration.alert.database.email.EmailConfigurationRepository;
import com.synopsys.integration.alert.database.email.properties.EmailConfigurationPropertiesRepository;
import com.synopsys.integration.alert.database.email.properties.EmailConfigurationsPropertyEntity;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

class EmailGlobalFieldModelSaveActionsTest {

    public static final String TEST_AUTH_REQUIRED = "true";
    public static final String TEST_FROM = "test.user@some.company.example.com";
    public static final String TEST_SMTP_HOST = "smtp.server.example.com";
    public static final String TEST_AUTH_PASSWORD = "apassword";
    public static final String TEST_SMTP_PORT = "2025";
    public static final String TEST_AUTH_USER = "auser";
    public static final String TEST_ADDITIONAL_PROPERTY = "mail.smtp.ehlo";

    private final Gson gson = new Gson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
    private final AuthorizationManager authorizationManager = createAuthorizationManager();
    private final EmailGlobalFieldModelConverter converter = new EmailGlobalFieldModelConverter();
    private final EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();

    @Test
    void getDescriptorKeyTest() {
        EmailGlobalFieldModelSaveActions saveActions = new EmailGlobalFieldModelSaveActions(null, null, null);
        assertEquals(ChannelKeys.EMAIL, saveActions.getDescriptorKey());
    }

    @Test
    void createTest() {
        AtomicReference<EmailConfigurationEntity> savedEntity = new AtomicReference<>();
        AtomicReference<EmailConfigurationsPropertyEntity> savedProperty = new AtomicReference<>();
        EmailConfigurationRepository emailConfigurationRepository = Mockito.mock(EmailConfigurationRepository.class);
        EmailConfigurationPropertiesRepository emailConfigurationPropertiesRepository = Mockito.mock(EmailConfigurationPropertiesRepository.class);
        Mockito.when(emailConfigurationRepository.save(Mockito.any(EmailConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });

        Mockito.when(emailConfigurationPropertiesRepository.saveAll(Mockito.any(List.class))).thenAnswer(invocation -> {
            Iterable<EmailConfigurationsPropertyEntity> iterable = invocation.getArgument(0);
            for (EmailConfigurationsPropertyEntity entity : iterable) {
                savedProperty.set(entity);
            }
            return List.of(savedProperty.get());
        });

        EmailGlobalConfigAccessor configurationAccessor = new EmailGlobalConfigAccessor(encryptionUtility, emailConfigurationRepository, emailConfigurationPropertiesRepository);
        EmailGlobalCrudActions crudActions = new EmailGlobalCrudActions(authorizationManager, configurationAccessor, validator);
        EmailGlobalFieldModelSaveActions saveActions = new EmailGlobalFieldModelSaveActions(converter, crudActions, configurationAccessor);
        saveActions.createConcreteModel(createDefaultFieldModel());
        EmailConfigurationEntity actualEntity = savedEntity.get();
        assertEquals(Boolean.TRUE, actualEntity.getAuthRequired());
        assertEquals(TEST_AUTH_USER, actualEntity.getAuthUsername());
        assertEquals(TEST_AUTH_PASSWORD, encryptionUtility.decrypt(actualEntity.getAuthPassword()));
        assertEquals(TEST_SMTP_HOST, actualEntity.getSmtpHost());
        assertEquals(Integer.valueOf(TEST_SMTP_PORT), actualEntity.getSmtpPort());
        assertEquals(TEST_FROM, actualEntity.getSmtpFrom());

        EmailConfigurationsPropertyEntity emailProperty = savedProperty.get();
        assertNotNull(emailProperty);
        assertEquals(TEST_ADDITIONAL_PROPERTY, emailProperty.getPropertyKey());
        assertEquals("true", emailProperty.getPropertyValue());
    }

    @Test
    void updateTest() {
        AtomicReference<EmailConfigurationEntity> savedEntity = new AtomicReference<>();
        AtomicReference<EmailConfigurationsPropertyEntity> savedProperty = new AtomicReference<>();
        EmailConfigurationRepository emailConfigurationRepository = Mockito.mock(EmailConfigurationRepository.class);
        EmailConfigurationPropertiesRepository emailConfigurationPropertiesRepository = Mockito.mock(EmailConfigurationPropertiesRepository.class);
        Mockito.when(emailConfigurationRepository.save(Mockito.any(EmailConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });

        Mockito.when(emailConfigurationRepository.findByName(Mockito.anyString())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));
        Mockito.when(emailConfigurationRepository.findById(Mockito.any())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));

        Mockito.when(emailConfigurationPropertiesRepository.saveAll(Mockito.any(List.class))).thenAnswer(invocation -> {
            Iterable<EmailConfigurationsPropertyEntity> iterable = invocation.getArgument(0);
            for (EmailConfigurationsPropertyEntity entity : iterable) {
                savedProperty.set(entity);
            }
            return List.of(savedProperty.get());
        });

        EmailGlobalConfigAccessor configurationAccessor = new EmailGlobalConfigAccessor(encryptionUtility, emailConfigurationRepository, emailConfigurationPropertiesRepository);
        EmailGlobalCrudActions crudActions = new EmailGlobalCrudActions(authorizationManager, configurationAccessor, validator);
        EmailGlobalFieldModelSaveActions saveActions = new EmailGlobalFieldModelSaveActions(converter, crudActions, configurationAccessor);
        String newPassword = "updatedPassword";
        String newHost = "updated." + TEST_SMTP_HOST;
        FieldModel defaultFieldModel = createDefaultFieldModel();
        saveActions.createConcreteModel(defaultFieldModel);
        defaultFieldModel.putField(EmailGlobalFieldModelConverter.EMAIL_HOST_KEY, new FieldValueModel(List.of(newHost), false));
        defaultFieldModel.putField(EmailGlobalFieldModelConverter.AUTH_PASSWORD_KEY, new FieldValueModel(List.of(newPassword), false));
        defaultFieldModel.putField(TEST_ADDITIONAL_PROPERTY, new FieldValueModel(List.of("false"), false));
        saveActions.updateConcreteModel(defaultFieldModel);
        EmailConfigurationEntity actualEntity = savedEntity.get();
        assertEquals(ChannelKeys.EMAIL, saveActions.getDescriptorKey());
        assertEquals(Boolean.TRUE, actualEntity.getAuthRequired());
        assertEquals(TEST_AUTH_USER, actualEntity.getAuthUsername());
        assertEquals(newPassword, encryptionUtility.decrypt(actualEntity.getAuthPassword()));
        assertEquals(newHost, actualEntity.getSmtpHost());
        assertEquals(Integer.valueOf(TEST_SMTP_PORT), actualEntity.getSmtpPort());
        assertEquals(TEST_FROM, actualEntity.getSmtpFrom());

        EmailConfigurationsPropertyEntity emailProperty = savedProperty.get();
        assertNotNull(emailProperty);
        assertEquals(TEST_ADDITIONAL_PROPERTY, emailProperty.getPropertyKey());
        assertEquals("false", emailProperty.getPropertyValue());
    }

    @Test
    void deleteTest() {
        AtomicReference<EmailConfigurationEntity> savedEntity = new AtomicReference<>();
        AtomicReference<EmailConfigurationsPropertyEntity> savedProperty = new AtomicReference<>();
        EmailConfigurationRepository emailConfigurationRepository = Mockito.mock(EmailConfigurationRepository.class);
        EmailConfigurationPropertiesRepository emailConfigurationPropertiesRepository = Mockito.mock(EmailConfigurationPropertiesRepository.class);
        Mockito.when(emailConfigurationRepository.save(Mockito.any(EmailConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });

        Mockito.when(emailConfigurationRepository.findByName(Mockito.anyString())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));
        Mockito.when(emailConfigurationRepository.findById(Mockito.any())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));
        Mockito.doAnswer(invocation -> {
            savedEntity.set(null);
            savedProperty.set(null);
            return null;
        }).when(emailConfigurationRepository).deleteById(Mockito.any());

        Mockito.when(emailConfigurationPropertiesRepository.saveAll(Mockito.any(List.class))).thenAnswer(invocation -> {
            Iterable<EmailConfigurationsPropertyEntity> iterable = invocation.getArgument(0);
            for (EmailConfigurationsPropertyEntity entity : iterable) {
                savedProperty.set(entity);
            }
            return List.of(savedProperty.get());
        });

        EmailGlobalConfigAccessor configurationAccessor = new EmailGlobalConfigAccessor(encryptionUtility, emailConfigurationRepository, emailConfigurationPropertiesRepository);
        EmailGlobalCrudActions crudActions = new EmailGlobalCrudActions(authorizationManager, configurationAccessor, validator);
        EmailGlobalFieldModelSaveActions saveActions = new EmailGlobalFieldModelSaveActions(converter, crudActions, configurationAccessor);
        FieldModel fieldModel = createDefaultFieldModel();
        saveActions.createConcreteModel(fieldModel);
        EmailConfigurationEntity actualEntity = savedEntity.get();
        EmailConfigurationsPropertyEntity actualPropertyEntity = savedProperty.get();
        assertNotNull(actualEntity);
        assertNotNull(actualPropertyEntity);
        saveActions.deleteConcreteModel(fieldModel);

        actualEntity = savedEntity.get();
        actualPropertyEntity = savedProperty.get();
        assertNull(actualEntity);
        assertNull(actualPropertyEntity);
    }

    private AuthorizationManager createAuthorizationManager() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }

    private FieldModel createDefaultFieldModel() {
        Map<String, FieldValueModel> fieldValuesMap = new HashMap<>();
        fieldValuesMap.put(EmailGlobalFieldModelConverter.EMAIL_FROM_KEY, new FieldValueModel(List.of(TEST_FROM), false));
        fieldValuesMap.put(EmailGlobalFieldModelConverter.EMAIL_HOST_KEY, new FieldValueModel(List.of(TEST_SMTP_HOST), false));
        fieldValuesMap.put(EmailGlobalFieldModelConverter.EMAIL_PORT_KEY, new FieldValueModel(List.of(TEST_SMTP_PORT), false));
        fieldValuesMap.put(EmailGlobalFieldModelConverter.AUTH_REQUIRED_KEY, new FieldValueModel(List.of(TEST_AUTH_REQUIRED), false));
        fieldValuesMap.put(EmailGlobalFieldModelConverter.AUTH_PASSWORD_KEY, new FieldValueModel(List.of(TEST_AUTH_PASSWORD), false));
        fieldValuesMap.put(EmailGlobalFieldModelConverter.AUTH_USER_KEY, new FieldValueModel(List.of(TEST_AUTH_USER), false));

        fieldValuesMap.put(TEST_ADDITIONAL_PROPERTY, new FieldValueModel(List.of("true"), false));
        return new FieldModel(ChannelKeys.EMAIL.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), fieldValuesMap);
    }
}
