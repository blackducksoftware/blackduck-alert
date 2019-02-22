package com.synopsys.integration.alert.channel.email;

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
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
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.database.api.AuditEntryUtility;
import com.synopsys.integration.alert.database.api.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.api.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckEmailHandler;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;

public class EmailGlobalDescriptorActionApiTest {

    @Test
    public void validateConfigEmptyTest() {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig();
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel);

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
        final EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig();
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel);
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
        final EmailGlobalUIConfig uiConfig = new EmailGlobalUIConfig();
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel);
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
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel);
        final Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        final FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);

        final TestConfigModel testConfigModel = new TestConfigModel(fieldAccessor);
        emailGlobalDescriptorActionApi.testConfig(testConfigModel);

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
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel);
        final Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        final FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldAccessor, "fake");
        try {
            emailGlobalDescriptorActionApi.testConfig(testConfigModel);
            fail("Should have thrown exception");
        } catch (final AlertException e) {
            assertTrue(e.getMessage().contains("fake is not a valid email address."));
        }
    }

    @Test
    public void testConfigWithValidDestinationTest() throws Exception {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel);

        final Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        final FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldAccessor, "fake@synopsys.com");

        emailGlobalDescriptorActionApi.testConfig(testConfigModel);

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
        final AuditEntryUtility auditUtility = Mockito.mock(AuditEntryUtility.class);

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
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel);

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

        emailGlobalDescriptorActionApi.testConfig(testConfigModel);

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
