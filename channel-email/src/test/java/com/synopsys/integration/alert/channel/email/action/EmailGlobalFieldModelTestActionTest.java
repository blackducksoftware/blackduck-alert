package com.synopsys.integration.alert.channel.email.action;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.attachment.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessagingService;
import com.synopsys.integration.alert.channel.email.distribution.address.JobEmailAddressValidator;
import com.synopsys.integration.alert.channel.email.distribution.address.ValidatedEmailAddresses;
import com.synopsys.integration.alert.common.action.FieldModelTestAction;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.service.email.EmailMessagingService;
import com.synopsys.integration.alert.service.email.JavamailPropertiesFactory;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;

public class EmailGlobalFieldModelTestActionTest {
    @Test
    public void testConfigValidTest() throws AlertException {
        EmailChannelMessagingService emailChannelMessagingService = Mockito.mock(EmailChannelMessagingService.class);
        Mockito.when(emailChannelMessagingService.sendMessage(Mockito.any(), Mockito.any())).thenReturn(new MessageResult("PASS"));

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailGlobalFieldModelTestAction emailGlobalFieldModelTestAction = new EmailGlobalFieldModelTestAction(emailChannelMessagingService, javamailPropertiesFactory);

        FieldModel validFieldModel = createFieldModelToTest("noreply@synopsys.com");

        try {
            MessageResult messageResult = emailGlobalFieldModelTestAction.testConfig("0", validFieldModel, new FieldUtility(Map.of()));
            assertFalse(messageResult.hasErrors(), "Expected the message result to not have errors");
            assertFalse(messageResult.hasWarnings(), "Expected the message result to not have warnings");
        } catch (AlertException e) {
            fail("An exception was thrown where none was expected", e);
        }
    }

    @Test
    public void testConfigMissingDestinationTest() {
        JobEmailAddressValidator emailAddressValidator = Mockito.mock(JobEmailAddressValidator.class);
        Mockito.when(emailAddressValidator.validate(Mockito.any(), Mockito.anyCollection())).thenReturn(new ValidatedEmailAddresses(Set.of(), Set.of()));

        MockAlertProperties testAlertProperties = new MockAlertProperties();
        MessageContentGroupCsvCreator messageContentGroupCsvCreator = new MessageContentGroupCsvCreator();
        Gson gson = new Gson();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(testAlertProperties, messageContentGroupCsvCreator, gson);

        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        EmailMessagingService emailMessagingService = new EmailMessagingService(freemarkerTemplatingService);

        EmailChannelMessagingService emailChannelMessagingService = new EmailChannelMessagingService(testAlertProperties, emailMessagingService, emailAttachmentFileCreator);

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();

        EmailGlobalFieldModelTestAction emailGlobalFieldModelTestAction = new EmailGlobalFieldModelTestAction(emailChannelMessagingService, javamailPropertiesFactory);
        FieldModel validFieldModel = createFieldModelToTest("");

        try {
            emailGlobalFieldModelTestAction.testConfig("0", validFieldModel, new FieldUtility(Map.of()));
            fail("Expected an exception to be thrown");
        } catch (AlertException e) {
            // Pass
        }
    }

    @Test
    public void testConfigInvalidDestinationTest() {
        EmailGlobalFieldModelTestAction emailGlobalFieldModelTestAction = new EmailGlobalFieldModelTestAction(null, null);

        FieldModel validFieldModel = createFieldModelToTest("not a valid email address");

        try {
            emailGlobalFieldModelTestAction.testConfig("0", validFieldModel, new FieldUtility(Map.of()));
            fail("Expected an exception to be thrown");
        } catch (AlertException e) {
            // Pass
        }
    }

    @Test
    @Tags(value = {
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    })
    public void testConfigITTest() {
        TestProperties testProperties = new TestProperties();
        String emailAddress = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        FieldModel validFieldModel = createFieldModelToTest(emailAddress);

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailChannelMessagingService validEmailChannelMessagingService = createValidEmailChannelMessagingService(emailAddress);
        EmailGlobalFieldModelTestAction emailGlobalFieldModelTestAction = new EmailGlobalFieldModelTestAction(validEmailChannelMessagingService, javamailPropertiesFactory);

        FieldUtility validFieldUtility = createValidEmailGlobalFieldUtility(testProperties);

        try {
            MessageResult messageResult = emailGlobalFieldModelTestAction.testConfig("0", validFieldModel, validFieldUtility);
            assertFalse(messageResult.hasErrors(), "Expected the message result to not have errors");
            assertFalse(messageResult.hasWarnings(), "Expected the message result to not have warnings");
        } catch (AlertException e) {
            fail("An exception was thrown where none was expected", e);
        }
    }

    private FieldModel createFieldModelToTest(String destinationValue) {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();

        Set<String> values = null != destinationValue ? Set.of(destinationValue) : Set.of();

        FieldValueModel destinationFieldValueModel = new FieldValueModel(values, false);
        keyToValues.put(FieldModelTestAction.KEY_DESTINATION_NAME, destinationFieldValueModel);

        return new FieldModel(null, null, keyToValues);
    }

    private EmailChannelMessagingService createValidEmailChannelMessagingService(String emailAddress) {
        MockAlertProperties testAlertProperties = new MockAlertProperties();

        JobEmailAddressValidator emailAddressValidator = Mockito.mock(JobEmailAddressValidator.class);
        Mockito.when(emailAddressValidator.validate(Mockito.any(), Mockito.anyCollection())).thenReturn(new ValidatedEmailAddresses(Set.of(emailAddress), Set.of()));

        Gson gson = new Gson();
        MessageContentGroupCsvCreator messageContentGroupCsvCreator = new MessageContentGroupCsvCreator();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(testAlertProperties, messageContentGroupCsvCreator, gson);
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        EmailMessagingService emailMessagingService = new EmailMessagingService(freemarkerTemplatingService);

        return new EmailChannelMessagingService(testAlertProperties, emailMessagingService, emailAttachmentFileCreator);
    }

    private FieldUtility createValidEmailGlobalFieldUtility(TestProperties testProperties) {
        Map<String, ConfigurationFieldModel> configuredFields = new HashMap<>();
        addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));

        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER).ifPresent(prop -> addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), prop));
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD).ifPresent(prop -> addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), prop));
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO).ifPresent(prop -> addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), prop));
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH).ifPresent(prop -> addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), prop));
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT).ifPresent(prop -> addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), prop));

        return new FieldUtility(configuredFields);
    }

    private void addConfigurationFieldToMap(Map<String, ConfigurationFieldModel> configuredFields, String key, String value) {
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(key);
        configurationFieldModel.setFieldValue(value);
        configuredFields.put(key, configurationFieldModel);
    }

}
