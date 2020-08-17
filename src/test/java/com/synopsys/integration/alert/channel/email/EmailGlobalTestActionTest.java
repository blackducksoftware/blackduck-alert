package com.synopsys.integration.alert.channel.email;

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
import static com.synopsys.integration.alert.util.FieldModelUtil.addFieldValueToMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.actions.EmailGlobalTestAction;
import com.synopsys.integration.alert.channel.email.descriptor.EmailGlobalUIConfig;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFormat;
import com.synopsys.integration.alert.channel.email.template.EmailChannelMessageParser;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionSettingsValidator;
import com.synopsys.integration.alert.common.email.EmailProperties;
import com.synopsys.integration.alert.common.email.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.alert.database.api.DefaultProviderDataAccessor;
import com.synopsys.integration.alert.util.AlertFieldStatusConverter;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.web.config.FieldValidationAction;

public class EmailGlobalTestActionTest {
    private static final EmailChannelKey EMAIL_CHANNEL_KEY = new EmailChannelKey();

    @Test
    public void validateConfigEmptyTest() {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        EncryptionSettingsValidator encryptionValidator = new EncryptionSettingsValidator(encryptionUtility);
        EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig(encryptionValidator);
        uiConfig.setConfigFields();

        FieldModel fieldModel = new FieldModel(EMAIL_CHANNEL_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(uiConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        List<AlertFieldStatus> fieldErrors = fieldValidationAction.validateConfig(configFieldMap, fieldModel);

        Map<String, String> fieldErrorMap = AlertFieldStatusConverter.convertToStringMap(fieldErrors);
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrorMap.get(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey()));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrorMap.get(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey()));
    }

    @Test
    public void validateConfigInvalidTest() {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        EncryptionSettingsValidator encryptionValidator = new EncryptionSettingsValidator(encryptionUtility);
        EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig(encryptionValidator);
        uiConfig.setConfigFields();
        Map<String, FieldValueModel> fields = new HashMap<>();
        fillMapBlanks(fields);
        addFieldValueToMap(fields, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "notInt");
        addFieldValueToMap(fields, EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "notInt");
        addFieldValueToMap(fields, EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "notInt");

        FieldModel fieldModel = new FieldModel(EMAIL_CHANNEL_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), fields);
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(uiConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        List<AlertFieldStatus> fieldErrors = fieldValidationAction.validateConfig(configFieldMap, fieldModel);

        Map<String, String> fieldErrorMap = AlertFieldStatusConverter.convertToStringMap(fieldErrors);
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrorMap.get(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrorMap.get(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrorMap.get(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey()));
    }

    @Test
    public void validateConfigValidTest() {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        EncryptionSettingsValidator encryptionValidator = new EncryptionSettingsValidator(encryptionUtility);
        EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig(encryptionValidator);
        uiConfig.setConfigFields();
        Map<String, FieldValueModel> fields = new HashMap<>();
        fillMap(fields);
        addFieldValueToMap(fields, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "10");
        addFieldValueToMap(fields, EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "25");
        addFieldValueToMap(fields, EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "30");

        FieldModel fieldModel = new FieldModel(EMAIL_CHANNEL_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), fields);
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(uiConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        List<AlertFieldStatus> fieldErrors = fieldValidationAction.validateConfig(configFieldMap, fieldModel);

        Map<String, String> fieldErrorMap = AlertFieldStatusConverter.convertToStringMap(fieldErrors);
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrorMap.get(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrorMap.get(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrorMap.get(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrorMap.get(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrorMap.get(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey()));
    }

    @Test
    public void testConfigEmptyTest() throws Exception {
        EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(emailChannel);
        Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);

        emailGlobalTestAction.testConfig(null, createFieldModel(null), fieldAccessor);

        ArgumentCaptor<EmailProperties> props = ArgumentCaptor.forClass(EmailProperties.class);
        ArgumentCaptor<Set> emailAddresses = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<String> subjectLine = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> format = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EmailAttachmentFormat> attachmentFormat = ArgumentCaptor.forClass(EmailAttachmentFormat.class);
        ArgumentCaptor<MessageContentGroup> content = ArgumentCaptor.forClass(MessageContentGroup.class);

        Mockito.verify(emailChannel).sendMessage(props.capture(), emailAddresses.capture(), subjectLine.capture(), format.capture(), attachmentFormat.capture(), content.capture());
        assertTrue(emailAddresses.getValue().isEmpty());
        assertEquals("Test from Alert", subjectLine.getValue());
        assertEquals("", format.getValue());
    }

    @Test
    public void testConfigWithInvalidDestinationTest() throws Exception {
        EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(emailChannel);
        Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);
        try {
            emailGlobalTestAction.testConfig(null, createFieldModel("fake"), fieldAccessor);
            fail("Should have thrown exception");
        } catch (AlertException e) {
            assertTrue(e.getMessage().contains("fake is not a valid email address."));
        }
    }

    @Test
    public void testConfigWithValidDestinationTest() throws Exception {
        EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(emailChannel);

        Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);

        emailGlobalTestAction.testConfig(null, createFieldModel("fake@synopsys.com"), fieldAccessor);

        ArgumentCaptor<EmailProperties> props = ArgumentCaptor.forClass(EmailProperties.class);
        ArgumentCaptor<Set> emailAddresses = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<String> subjectLine = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> format = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EmailAttachmentFormat> attachmentFormat = ArgumentCaptor.forClass(EmailAttachmentFormat.class);
        ArgumentCaptor<MessageContentGroup> content = ArgumentCaptor.forClass(MessageContentGroup.class);

        Mockito.verify(emailChannel).sendMessage(props.capture(), emailAddresses.capture(), subjectLine.capture(), format.capture(), attachmentFormat.capture(), content.capture());
        assertEquals("fake@synopsys.com", emailAddresses.getValue().iterator().next());
        assertEquals("Test from Alert", subjectLine.getValue());
        assertEquals("", format.getValue());
    }

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void testConfigITTest() throws Exception {
        TestProperties properties = new TestProperties();
        DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);

        TestAlertProperties testAlertProperties = new TestAlertProperties();

        EmailAddressHandler emailAddressHandler = new EmailAddressHandler(Mockito.mock(DefaultProviderDataAccessor.class));
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        EmailChannelMessageParser emailChannelMessageParser = new EmailChannelMessageParser();

        Gson gson = new Gson();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(testAlertProperties, new MessageContentGroupCsvCreator(), gson);
        EmailChannel emailChannel = new EmailChannel(new EmailChannelKey(), gson, testAlertProperties, auditUtility, emailAddressHandler, freemarkerTemplatingService, emailChannelMessageParser, emailAttachmentFileCreator);
        //////////////////////////////////////
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(emailChannel);

        Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);
        emailGlobalTestAction.testConfig(null, createFieldModel(properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT)), fieldAccessor);

    }

    private FieldModel createFieldModel(String emailAddress) {
        List<String> addresses = List.of();
        if (null != emailAddress) {
            addresses = List.of(emailAddress);
        }

        String descriptorName = new EmailChannelKey().getUniversalKey();
        String context = ConfigContextEnum.GLOBAL.name();
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(TestAction.KEY_DESTINATION_NAME, new FieldValueModel(addresses, false));
        return new FieldModel(descriptorName, context, keyToValues);
    }

    private void fillMapBlanks(Map<String, FieldValueModel> fields) {
        for (EmailPropertyKeys value : EmailPropertyKeys.values()) {
            addFieldValueToMap(fields, value.getPropertyKey(), "");
        }
    }

    private void fillMap(Map<String, FieldValueModel> fields) {
        for (EmailPropertyKeys value : EmailPropertyKeys.values()) {
            addFieldValueToMap(fields, value.getPropertyKey(), "notempty");
        }
    }

}
