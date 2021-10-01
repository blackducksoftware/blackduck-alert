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
import com.synopsys.integration.alert.channel.email.distribution.address.EmailAddressGatherer;
import com.synopsys.integration.alert.channel.email.distribution.address.JobEmailAddressValidator;
import com.synopsys.integration.alert.channel.email.distribution.address.ValidatedEmailAddresses;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.rest.api.ConfigurationTestHelper;
import com.synopsys.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.service.email.EmailMessagingService;
import com.synopsys.integration.alert.service.email.JavamailPropertiesFactory;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.service.email.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;

public class EmailGlobalTestActionTest {
    @Test
    public void testConfigValidTest() throws AlertException {
        RoleAccessor roleAccessor = Mockito.mock(RoleAccessor.class);
        Mockito.when(roleAccessor.getRoles()).thenReturn(Set.of());
        AuthorizationManager authorizationManager = new AuthorizationManager(roleAccessor);
        ConfigurationValidationHelper validationHelper = new ConfigurationValidationHelper(authorizationManager);
        ConfigurationTestHelper testHelper = new ConfigurationTestHelper(authorizationManager);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailChannelMessagingService emailChannelMessagingService = Mockito.mock(EmailChannelMessagingService.class);
        Mockito.when(emailChannelMessagingService.sendMessage(Mockito.any(), Mockito.any())).thenReturn(new MessageResult("PASS"));

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(testHelper, validationHelper, validator, emailChannelMessagingService, javamailPropertiesFactory);

        try {
            MessageResult messageResult = emailGlobalTestAction.testConfigModelContent("noreply@synopsys.com", new EmailGlobalConfigModel());
            assertFalse(messageResult.hasErrors(), "Expected the message result to not have errors");
            assertFalse(messageResult.hasWarnings(), "Expected the message result to not have warnings");
        } catch (AlertException e) {
            fail("An exception was thrown where none was expected", e);
        }
    }

    @Test
    public void testConfigMissingDestinationTest() {
        RoleAccessor roleAccessor = Mockito.mock(RoleAccessor.class);
        Mockito.when(roleAccessor.getRoles()).thenReturn(Set.of());
        AuthorizationManager authorizationManager = new AuthorizationManager(roleAccessor);
        ConfigurationValidationHelper validationHelper = new ConfigurationValidationHelper(authorizationManager);
        ConfigurationTestHelper testHelper = new ConfigurationTestHelper(authorizationManager);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailAddressGatherer emailAddressGatherer = Mockito.mock(EmailAddressGatherer.class);
        Mockito.when(emailAddressGatherer.gatherEmailAddresses(Mockito.any(), Mockito.any())).thenReturn(Set.of());

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

        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(testHelper, validationHelper, validator, emailChannelMessagingService, javamailPropertiesFactory);

        try {
            emailGlobalTestAction.testConfigModelContent("", new EmailGlobalConfigModel());
            fail("Expected an exception to be thrown");
        } catch (AlertException e) {
            // Pass
        }
    }

    @Test
    public void testConfigInvalidDestinationTest() {
        RoleAccessor roleAccessor = Mockito.mock(RoleAccessor.class);
        Mockito.when(roleAccessor.getRoles()).thenReturn(Set.of());
        AuthorizationManager authorizationManager = new AuthorizationManager(roleAccessor);
        ConfigurationValidationHelper validationHelper = new ConfigurationValidationHelper(authorizationManager);
        ConfigurationTestHelper testHelper = new ConfigurationTestHelper(authorizationManager);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(testHelper, validationHelper, validator, null, null);

        try {
            emailGlobalTestAction.testConfigModelContent("not a valid email address", new EmailGlobalConfigModel());
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
        RoleAccessor roleAccessor = Mockito.mock(RoleAccessor.class);
        Mockito.when(roleAccessor.getRoles()).thenReturn(Set.of());
        AuthorizationManager authorizationManager = new AuthorizationManager(roleAccessor);
        ConfigurationValidationHelper validationHelper = new ConfigurationValidationHelper(authorizationManager);
        ConfigurationTestHelper testHelper = new ConfigurationTestHelper(authorizationManager);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        TestProperties testProperties = new TestProperties();
        String emailAddress = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailChannelMessagingService validEmailChannelMessagingService = createValidEmailChannelMessagingService(emailAddress);
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(testHelper, validationHelper, validator, validEmailChannelMessagingService, javamailPropertiesFactory);

        EmailGlobalConfigModel globalConfigModel = createValidEmailGlobalConfigModel(testProperties);

        try {
            MessageResult messageResult = emailGlobalTestAction.testConfigModelContent(emailAddress, globalConfigModel);
            assertFalse(messageResult.hasErrors(), "Expected the message result to not have errors");
            assertFalse(messageResult.hasWarnings(), "Expected the message result to not have warnings");
        } catch (AlertException e) {
            fail("An exception was thrown where none was expected", e);
        }
    }

    private EmailChannelMessagingService createValidEmailChannelMessagingService(String emailAddress) {
        MockAlertProperties testAlertProperties = new MockAlertProperties();

        EmailAddressGatherer emailAddressGatherer = Mockito.mock(EmailAddressGatherer.class);
        Mockito.when(emailAddressGatherer.gatherEmailAddresses(Mockito.any(), Mockito.any())).thenReturn(Set.of(emailAddress));

        JobEmailAddressValidator emailAddressValidator = Mockito.mock(JobEmailAddressValidator.class);
        Mockito.when(emailAddressValidator.validate(Mockito.any(), Mockito.anyCollection())).thenReturn(new ValidatedEmailAddresses(Set.of(emailAddress), Set.of()));

        Gson gson = new Gson();
        MessageContentGroupCsvCreator messageContentGroupCsvCreator = new MessageContentGroupCsvCreator();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(testAlertProperties, messageContentGroupCsvCreator, gson);
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        EmailMessagingService emailMessagingService = new EmailMessagingService(freemarkerTemplatingService);

        return new EmailChannelMessagingService(testAlertProperties, emailMessagingService, emailAttachmentFileCreator);
    }

    private EmailGlobalConfigModel createValidEmailGlobalConfigModel(TestProperties testProperties) {
        EmailGlobalConfigModel emailGlobalConfigModel = new EmailGlobalConfigModel();
        emailGlobalConfigModel.setFrom(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        emailGlobalConfigModel.setHost(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT).map(Integer::valueOf).ifPresent(emailGlobalConfigModel::setPort);

        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH).map(Boolean::valueOf).ifPresent(emailGlobalConfigModel::setAuth);
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER).ifPresent(emailGlobalConfigModel::setUsername);
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD).ifPresent(emailGlobalConfigModel::setPassword);

        Map<String, String> additionalPropertiesMap = new HashMap<>();
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO).ifPresent(prop -> additionalPropertiesMap.put(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), prop));

        emailGlobalConfigModel.setAdditionalJavaMailProperties(additionalPropertiesMap);

        return emailGlobalConfigModel;
    }

}
