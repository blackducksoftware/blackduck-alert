package com.synopsys.integration.alert.channel.email;

import static com.synopsys.integration.alert.util.FieldModelUtil.addFieldValueToMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.descriptor.EmailGlobalDescriptorActionApi;
import com.synopsys.integration.alert.channel.email.descriptor.EmailGlobalUIConfig;
import com.synopsys.integration.alert.common.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckEmailHandler;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;

public class EmailGlobalDescriptorActionApiTest {

    @Test
    public void validateConfigEmptyTest() {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel, modelConverter);
        final EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig();
        final FieldModel fieldModel = new FieldModel(EmailChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), Map.of());
        final Map<String, String> fieldErrors = new HashMap<>();

        final Map<String, ConfigField> configFieldMap = uiConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        emailGlobalDescriptorActionApi.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey()));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey()));
    }

    @Test
    public void validateConfigInvalidTest() {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel, modelConverter);
        final EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig();
        final Map<String, FieldValueModel> fields = new HashMap<>();
        fillMapBlanks(fields);
        addFieldValueToMap(fields, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "notInt");
        addFieldValueToMap(fields, EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "notInt");
        addFieldValueToMap(fields, EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "notInt");

        final FieldModel fieldModel = new FieldModel(EmailChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), fields);
        final Map<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = uiConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        emailGlobalDescriptorActionApi.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey()));
    }

    @Test
    public void validateConfigValidTest() {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel, modelConverter);
        final EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig();
        final Map<String, FieldValueModel> fields = new HashMap<>();
        fillMap(fields);
        addFieldValueToMap(fields, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "10");
        addFieldValueToMap(fields, EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "25");
        addFieldValueToMap(fields, EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "30");

        final FieldModel fieldModel = new FieldModel(EmailChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), fields);
        final Map<String, String> fieldErrors = new HashMap<>();

        final Map<String, ConfigField> configFieldMap = uiConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        emailGlobalDescriptorActionApi.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey()));
    }

    @Test
    public void testConfigEmptyTest() throws Exception {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel, modelConverter);
        final EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig();
        final Map<String, FieldValueModel> keyToValues = new HashMap<>();
        final FieldModel fieldModel = new FieldModel(EmailChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel);
        final Map<String, ConfigField> configFieldMap = uiConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        emailGlobalDescriptorActionApi.testConfig(configFieldMap, testConfigModel);

        final ArgumentCaptor<EmailProperties> props = ArgumentCaptor.forClass(EmailProperties.class);
        final ArgumentCaptor<Set> emailAddresses = ArgumentCaptor.forClass(Set.class);
        final ArgumentCaptor<String> subjectLine = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> provider = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> format = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<AggregateMessageContent> content = ArgumentCaptor.forClass(AggregateMessageContent.class);

        Mockito.verify(emailChannel).sendMessage(props.capture(), emailAddresses.capture(), subjectLine.capture(), provider.capture(), format.capture(), content.capture());
        assertTrue(emailAddresses.getValue().isEmpty());
        assertEquals("Test from Alert", subjectLine.getValue());
        assertEquals("Global Configuration", provider.getValue());
        assertEquals("", format.getValue());
    }

    @Test
    public void testConfigWithInvalidDestinationTest() throws Exception {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel, modelConverter);
        final EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig();
        final Map<String, FieldValueModel> keyToValues = new HashMap<>();
        final FieldModel fieldModel = new FieldModel(EmailChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel, "fake");
        try {
            final Map<String, ConfigField> configFieldMap = uiConfig.createFields().stream()
                                                                .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
            emailGlobalDescriptorActionApi.testConfig(configFieldMap, testConfigModel);
            fail("Should have thrown exception");
        } catch (final AlertException e) {
            assertTrue(e.getMessage().contains("fake is not a valid email address."));
        }
    }

    @Test
    public void testConfigWithValidDestinationTest() throws Exception {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel, modelConverter);
        final EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig();
        final Map<String, FieldValueModel> keyToValues = new HashMap<>();
        final FieldModel fieldModel = new FieldModel(EmailChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel, "fake@synopsys.com");
        final Map<String, ConfigField> configFieldMap = uiConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        emailGlobalDescriptorActionApi.testConfig(configFieldMap, testConfigModel);

        final ArgumentCaptor<EmailProperties> props = ArgumentCaptor.forClass(EmailProperties.class);
        final ArgumentCaptor<Set> emailAddresses = ArgumentCaptor.forClass(Set.class);
        final ArgumentCaptor<String> subjectLine = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> provider = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> format = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<AggregateMessageContent> content = ArgumentCaptor.forClass(AggregateMessageContent.class);

        Mockito.verify(emailChannel).sendMessage(props.capture(), emailAddresses.capture(), subjectLine.capture(), provider.capture(), format.capture(), content.capture());
        assertEquals("fake@synopsys.com", emailAddresses.getValue().iterator().next());
        assertEquals("Test from Alert", subjectLine.getValue());
        assertEquals("Global Configuration", provider.getValue());
        assertEquals("", format.getValue());
    }

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void testConfigITTest() throws Exception {
        final TestProperties properties = new TestProperties();
        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);

        final TestAlertProperties testAlertProperties = new TestAlertProperties();

        final BlackDuckEmailHandler blackDuckEmailHandler = new BlackDuckEmailHandler(Mockito.mock(BlackDuckProjectRepositoryAccessor.class), Mockito.mock(UserProjectRelationRepositoryAccessor.class), Mockito.mock(
            BlackDuckUserRepositoryAccessor.class));
        final BlackDuckProvider blackDuckProvider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(blackDuckProvider.getEmailHandler()).thenReturn(blackDuckEmailHandler);

        final BlackDuckDescriptor blackDuckDescriptor = Mockito.mock(BlackDuckDescriptor.class);
        Mockito.when(blackDuckDescriptor.getProvider()).thenReturn(blackDuckProvider);

        final DescriptorMap descriptorMap = Mockito.mock(DescriptorMap.class);
        Mockito.when(descriptorMap.getProviderDescriptor(Mockito.anyString())).thenReturn(Optional.of(blackDuckDescriptor));

        final EmailAddressHandler emailAddressHandler = new EmailAddressHandler(descriptorMap);

        final EmailChannel emailChannel = new EmailChannel(new Gson(), testAlertProperties, null, auditUtility, emailAddressHandler);
        //////////////////////////////////////
        final EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig();
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel, modelConverter);

        final Map<String, FieldValueModel> keyToValues = new HashMap<>();
        addFieldValueToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        addFieldValueToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        addFieldValueToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        addFieldValueToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));
        addFieldValueToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        addFieldValueToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        addFieldValueToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        final FieldModel fieldModel = new FieldModel(EmailChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel, properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT));
        final Map<String, ConfigField> configFieldMap = uiConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        emailGlobalDescriptorActionApi.testConfig(configFieldMap, testConfigModel);

    }

    private void fillMapBlanks(final Map<String, FieldValueModel> fields) {
        for (final EmailPropertyKeys value : EmailPropertyKeys.values()) {
            addFieldValueToMap(fields, value.getPropertyKey(), "");
        }
    }

    private void fillMap(final Map<String, FieldValueModel> fields) {
        for (final EmailPropertyKeys value : EmailPropertyKeys.values()) {
            addFieldValueToMap(fields, value.getPropertyKey(), "notempty");
        }
    }

}
