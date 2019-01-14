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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.descriptor.EmailGlobalDescriptorActionApi;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
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
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel);

        final FieldAccessor fieldAccessor = new FieldAccessor(new HashMap<>());
        final Map<String, String> fieldErrors = new HashMap<>();

        emailGlobalDescriptorActionApi.validateConfig(fieldAccessor, fieldErrors);
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void validateConfigInvalidTest() {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel);

        final Map<String, ConfigurationFieldModel> fields = new HashMap<>();
        fillMapBlanks(fields);
        addConfigurationFieldToMap(fields, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "notInt");
        addConfigurationFieldToMap(fields, EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "notInt");
        addConfigurationFieldToMap(fields, EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "notInt");

        final FieldAccessor fieldAccessor = new FieldAccessor(fields);
        final Map<String, String> fieldErrors = new HashMap<>();

        emailGlobalDescriptorActionApi.validateConfig(fieldAccessor, fieldErrors);
        assertEquals(EmailGlobalDescriptorActionApi.NOT_AN_INTEGER, fieldErrors.get("mailSmtpPort"));
        assertEquals(EmailGlobalDescriptorActionApi.NOT_AN_INTEGER, fieldErrors.get("mailSmtpConnectionTimeout"));
        assertEquals(EmailGlobalDescriptorActionApi.NOT_AN_INTEGER, fieldErrors.get("mailSmtpTimeout"));
    }

    @Test
    public void validateConfigValidTest() {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel);

        final Map<String, ConfigurationFieldModel> fields = new HashMap<>();
        fillMap(fields);
        addConfigurationFieldToMap(fields, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "10");
        addConfigurationFieldToMap(fields, EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "25");
        addConfigurationFieldToMap(fields, EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "30");

        final FieldAccessor fieldAccessor = new FieldAccessor(fields);
        final Map<String, String> fieldErrors = new HashMap<>();

        emailGlobalDescriptorActionApi.validateConfig(fieldAccessor, fieldErrors);
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testConfigEmptyTest() throws Exception {
        final EmailChannel emailChannel = Mockito.mock(EmailChannel.class);
        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel);

        final Map<String, FieldValueModel> keyToValues = new HashMap<>();
        final FieldModel fieldModel = new FieldModel(EmailChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel);
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

        final Map<String, FieldValueModel> keyToValues = new HashMap<>();
        final FieldModel fieldModel = new FieldModel(EmailChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel, "fake");
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

        final Map<String, FieldValueModel> keyToValues = new HashMap<>();
        final FieldModel fieldModel = new FieldModel(EmailChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel, "fake@synopsys.com");
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

        final EmailGlobalDescriptorActionApi emailGlobalDescriptorActionApi = new EmailGlobalDescriptorActionApi(emailChannel);

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
        emailGlobalDescriptorActionApi.testConfig(testConfigModel);

    }

    private void fillMapBlanks(final Map<String, ConfigurationFieldModel> fields) {
        for (final EmailPropertyKeys value : EmailPropertyKeys.values()) {
            addConfigurationFieldToMap(fields, value.getPropertyKey(), "");
        }
    }

    private void fillMap(final Map<String, ConfigurationFieldModel> fields) {
        for (final EmailPropertyKeys value : EmailPropertyKeys.values()) {
            addConfigurationFieldToMap(fields, value.getPropertyKey(), "notempty");
        }
    }

}
