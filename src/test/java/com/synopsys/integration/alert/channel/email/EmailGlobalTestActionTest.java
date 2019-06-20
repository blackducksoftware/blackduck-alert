package com.synopsys.integration.alert.channel.email;

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
import static com.synopsys.integration.alert.util.FieldModelUtil.addFieldValueToMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.actions.EmailGlobalTestAction;
import com.synopsys.integration.alert.channel.email.descriptor.EmailGlobalUIConfig;
import com.synopsys.integration.alert.channel.util.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.alert.database.api.DefaultProviderDataAccessor;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.web.config.FieldValidationAction;

public class EmailGlobalTestActionTest {

    @Test
    public void validateConfigEmptyTest() {
        final EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig();

        final FieldModel fieldModel = new FieldModel(EmailChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), Map.of());
        final Map<String, String> fieldErrors = new HashMap<>();

        final Map<String, ConfigField> configFieldMap = uiConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey()));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey()));
    }

    @Test
    public void validateConfigInvalidTest() {
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
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey()));
    }

    @Test
    public void validateConfigValidTest() {
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
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey()));
        assertEquals(NumberConfigField.NOT_AN_INTEGER_VALUE, fieldErrors.get(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey()));
    }

    @Test
    public void testConfigEmptyTest() throws Exception {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(emailChannel);
        final Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        final FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);

        final TestConfigModel testConfigModel = new TestConfigModel(fieldAccessor);
        emailGlobalTestAction.testConfig(testConfigModel);

        final ArgumentCaptor<EmailProperties> props = ArgumentCaptor.forClass(EmailProperties.class);
        final ArgumentCaptor<Set> emailAddresses = ArgumentCaptor.forClass(Set.class);
        final ArgumentCaptor<String> subjectLine = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> format = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<MessageContentGroup> content = ArgumentCaptor.forClass(MessageContentGroup.class);

        Mockito.verify(emailChannel).sendMessage(props.capture(), emailAddresses.capture(), subjectLine.capture(), format.capture(), content.capture());
        assertTrue(emailAddresses.getValue().isEmpty());
        assertEquals("Test from Alert", subjectLine.getValue());
        assertEquals("", format.getValue());
    }

    @Test
    public void testConfigWithInvalidDestinationTest() throws Exception {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(emailChannel);
        final Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        final FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldAccessor, "fake");
        try {
            emailGlobalTestAction.testConfig(testConfigModel);
            fail("Should have thrown exception");
        } catch (final AlertException e) {
            assertTrue(e.getMessage().contains("fake is not a valid email address."));
        }
    }

    @Test
    public void testConfigWithValidDestinationTest() throws Exception {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(emailChannel);

        final Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        final FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldAccessor, "fake@synopsys.com");

        emailGlobalTestAction.testConfig(testConfigModel);

        final ArgumentCaptor<EmailProperties> props = ArgumentCaptor.forClass(EmailProperties.class);
        final ArgumentCaptor<Set> emailAddresses = ArgumentCaptor.forClass(Set.class);
        final ArgumentCaptor<String> subjectLine = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> format = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<MessageContentGroup> content = ArgumentCaptor.forClass(MessageContentGroup.class);

        Mockito.verify(emailChannel).sendMessage(props.capture(), emailAddresses.capture(), subjectLine.capture(), format.capture(), content.capture());
        assertEquals("fake@synopsys.com", emailAddresses.getValue().iterator().next());
        assertEquals("Test from Alert", subjectLine.getValue());
        assertEquals("", format.getValue());
    }

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void testConfigITTest() throws Exception {
        final TestProperties properties = new TestProperties();
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);

        final TestAlertProperties testAlertProperties = new TestAlertProperties();

        final EmailAddressHandler emailAddressHandler = new EmailAddressHandler(Mockito.mock(DefaultProviderDataAccessor.class));
        final FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(testAlertProperties);
        final EmailChannel emailChannel = new EmailChannel(new Gson(), testAlertProperties, null, null, auditUtility, emailAddressHandler, freemarkerTemplatingService);
        //////////////////////////////////////
        final EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(emailChannel);

        final Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        addConfigurationFieldToMap(keyToValues, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        final FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldAccessor, properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT));

        emailGlobalTestAction.testConfig(testConfigModel);

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
