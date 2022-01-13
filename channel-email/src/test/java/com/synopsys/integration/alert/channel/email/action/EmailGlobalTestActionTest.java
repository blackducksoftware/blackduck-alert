package com.synopsys.integration.alert.channel.email.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.attachment.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessagingService;
import com.synopsys.integration.alert.channel.email.distribution.address.EmailAddressGatherer;
import com.synopsys.integration.alert.channel.email.distribution.address.JobEmailAddressValidator;
import com.synopsys.integration.alert.channel.email.distribution.address.ValidatedEmailAddresses;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.ConfigurationTestResult;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.service.email.EmailMessagingService;
import com.synopsys.integration.alert.service.email.JavamailPropertiesFactory;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.service.email.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;

public class EmailGlobalTestActionTest {
    private EmailGlobalConfigAccessor configurationAccessor;

    @BeforeEach
    public void init() {
        configurationAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
    }

    @Test
    public void testConfigValidTest() throws AlertException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailChannelMessagingService emailChannelMessagingService = Mockito.mock(EmailChannelMessagingService.class);
        Mockito.when(emailChannelMessagingService.sendMessage(Mockito.any(), Mockito.any())).thenReturn(new MessageResult("PASS"));

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, emailChannelMessagingService, javamailPropertiesFactory, configurationAccessor);

        ConfigurationTestResult testResult = emailGlobalTestAction.testConfigModelContent("noreply@synopsys.com", new EmailGlobalConfigModel());
        assertTrue(testResult.isSuccess(), "Expected the message result to not have errors");
    }

    @Test
    public void testConfigMissingDestinationTest() {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
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

        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, emailChannelMessagingService, javamailPropertiesFactory, configurationAccessor);

        ConfigurationTestResult testResult = emailGlobalTestAction.testConfigModelContent("", new EmailGlobalConfigModel());
        assertFalse(testResult.isSuccess());
    }

    @Test
    public void testConfigInvalidDestinationTest() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, null, null, null);

        ConfigurationTestResult testResult = emailGlobalTestAction.testConfigModelContent("not a valid email address", new EmailGlobalConfigModel());
        assertFalse(testResult.isSuccess());
    }

    @Test
    public void testSmtpPasswordMissingTest() throws AlertException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailChannelMessagingService emailChannelMessagingService = Mockito.mock(EmailChannelMessagingService.class);
        Mockito.when(emailChannelMessagingService.sendMessage(Mockito.any(), Mockito.any())).thenReturn(new MessageResult("PASS"));

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, emailChannelMessagingService, javamailPropertiesFactory, configurationAccessor);

        EmailGlobalConfigModel emailGlobalConfigModel = new EmailGlobalConfigModel();
        emailGlobalConfigModel.setIsSmtpPasswordSet(true);
        EmailGlobalConfigModel configModelWithPassword = new EmailGlobalConfigModel();
        configModelWithPassword.setIsSmtpPasswordSet(true);
        configModelWithPassword.setSmtpPassword("password");
        Mockito.when(configurationAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(configModelWithPassword));

        ConfigurationTestResult testResult = emailGlobalTestAction.testConfigModelContent("noreply@synopsys.com", emailGlobalConfigModel);
        assertTrue(testResult.isSuccess(), "Expected the message result to not have errors");
    }

    @Test
    @Tags(value = {
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    })
    public void testSmtpPasswordMissingTestIT() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        TestProperties testProperties = new TestProperties();
        String emailAddress = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        assumeTrue(testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD).isPresent());

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailChannelMessagingService validEmailChannelMessagingService = createValidEmailChannelMessagingService(emailAddress);
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, validEmailChannelMessagingService, javamailPropertiesFactory, configurationAccessor);

        EmailGlobalConfigModel globalConfigModelWithoutPassword = createEmailGlobalConfigModelObfuscated(testProperties);
        EmailGlobalConfigModel globalConfigModelWithPassword = createValidEmailGlobalConfigModel(testProperties);
        Mockito.when(configurationAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(globalConfigModelWithPassword));

        ConfigurationTestResult testResult = emailGlobalTestAction.testConfigModelContent(emailAddress, globalConfigModelWithoutPassword);
        assertTrue(testResult.isSuccess(), "Expected the message result to not have errors");
    }

    @Test
    @Tags(value = {
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    })
    public void testConfigITTest() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        TestProperties testProperties = new TestProperties();
        String emailAddress = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailChannelMessagingService validEmailChannelMessagingService = createValidEmailChannelMessagingService(emailAddress);
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, validEmailChannelMessagingService, javamailPropertiesFactory, configurationAccessor);

        EmailGlobalConfigModel globalConfigModel = createValidEmailGlobalConfigModel(testProperties);

        ConfigurationTestResult testResult = emailGlobalTestAction.testConfigModelContent(emailAddress, globalConfigModel);
        assertTrue(testResult.isSuccess(), "Expected the message result to not have errors");
    }

    @Test
    public void testPermissionForbiddenTest() throws AlertException {
        AuthorizationManager authorizationManager = createAuthorizationManager(0);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailChannelMessagingService emailChannelMessagingService = Mockito.mock(EmailChannelMessagingService.class);
        Mockito.when(emailChannelMessagingService.sendMessage(Mockito.any(), Mockito.any())).thenReturn(new MessageResult("PASS"));

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, emailChannelMessagingService, javamailPropertiesFactory, configurationAccessor);

        ActionResponse<ValidationResponseModel> response = emailGlobalTestAction.testWithPermissionCheck("noreply@synopsys.com", new EmailGlobalConfigModel());
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    public void testPermissionConfigValidTest() throws AlertException {
        TestProperties testProperties = new TestProperties();
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailChannelMessagingService emailChannelMessagingService = Mockito.mock(EmailChannelMessagingService.class);
        Mockito.when(emailChannelMessagingService.sendMessage(Mockito.any(), Mockito.any())).thenReturn(new MessageResult("PASS"));

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, emailChannelMessagingService, javamailPropertiesFactory, configurationAccessor);

        EmailGlobalConfigModel globalConfigModel = createValidEmailGlobalConfigModel(testProperties);

        ActionResponse<ValidationResponseModel> response = emailGlobalTestAction.testWithPermissionCheck("noreply@synopsys.com", globalConfigModel);
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertFalse(response.getContent().get().hasErrors(), "Expected the message result to not have errors");
    }

    @Test
    public void testPermissionConfigMissingDestinationTest() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
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

        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, emailChannelMessagingService, javamailPropertiesFactory, configurationAccessor);

        ActionResponse<ValidationResponseModel> response = emailGlobalTestAction.testWithPermissionCheck("", new EmailGlobalConfigModel());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(response.hasContent());
        assertTrue(response.getContent().get().hasErrors(), "Expected the message result to not have errors");

    }

    @Test
    public void testPermissionConfigInvalidDestinationTest() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, null, null, configurationAccessor);

        ActionResponse<ValidationResponseModel> response = emailGlobalTestAction.testWithPermissionCheck("not a valid email address", new EmailGlobalConfigModel());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(response.hasContent());
        assertTrue(response.getContent().get().hasErrors(), "Expected the message result to not have errors");
    }

    @Test
    @Tags(value = {
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    })
    public void testPermissionConfigITTest() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        TestProperties testProperties = new TestProperties();
        String emailAddress = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailChannelMessagingService validEmailChannelMessagingService = createValidEmailChannelMessagingService(emailAddress);
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, validEmailChannelMessagingService, javamailPropertiesFactory, configurationAccessor);

        EmailGlobalConfigModel globalConfigModel = createValidEmailGlobalConfigModel(testProperties);

        ActionResponse<ValidationResponseModel> response = emailGlobalTestAction.testWithPermissionCheck(emailAddress, globalConfigModel);
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(response.hasContent());
        assertFalse(response.getContent().get().hasErrors(), "Expected the message result to not have errors");
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

    private AuthorizationManager createAuthorizationManager(int assignedPermissions) {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, assignedPermissions);
        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }

    private EmailGlobalConfigModel createValidEmailGlobalConfigModel(TestProperties testProperties) {
        EmailGlobalConfigModel emailGlobalConfigModel = new EmailGlobalConfigModel();
        emailGlobalConfigModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        emailGlobalConfigModel.setSmtpFrom(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        emailGlobalConfigModel.setSmtpHost(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT).map(Integer::valueOf).ifPresent(emailGlobalConfigModel::setSmtpPort);

        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH).map(Boolean::valueOf).ifPresent(emailGlobalConfigModel::setSmtpAuth);
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER).ifPresent(emailGlobalConfigModel::setSmtpUsername);
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD).ifPresent(emailGlobalConfigModel::setSmtpPassword);

        Map<String, String> additionalPropertiesMap = new HashMap<>();
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO).ifPresent(prop -> additionalPropertiesMap.put(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), prop));

        emailGlobalConfigModel.setAdditionalJavaMailProperties(additionalPropertiesMap);

        return emailGlobalConfigModel;
    }

    private EmailGlobalConfigModel createEmailGlobalConfigModelObfuscated(TestProperties testProperties) {
        EmailGlobalConfigModel emailGlobalConfigModel = createValidEmailGlobalConfigModel(testProperties);
        if (testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD).isPresent()) {
            emailGlobalConfigModel.setIsSmtpPasswordSet(true);
            emailGlobalConfigModel.setSmtpPassword(null);
        }

        return emailGlobalConfigModel;
    }

}
