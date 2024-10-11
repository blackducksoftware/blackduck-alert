/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.channel.email.attachment.EmailAttachmentFileCreator;
import com.blackduck.integration.alert.channel.email.attachment.MessageContentGroupCsvCreator;
import com.blackduck.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.email.distribution.EmailChannelMessagingService;
import com.blackduck.integration.alert.channel.email.distribution.address.EmailAddressGatherer;
import com.blackduck.integration.alert.channel.email.distribution.address.JobEmailAddressValidator;
import com.blackduck.integration.alert.channel.email.distribution.address.ValidatedEmailAddresses;
import com.blackduck.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.message.model.ConfigurationTestResult;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.service.email.EmailMessagingService;
import com.blackduck.integration.alert.service.email.JavamailPropertiesFactory;
import com.blackduck.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.blackduck.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.blackduck.integration.alert.service.email.template.FreemarkerTemplatingService;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

class EmailGlobalTestActionTest {
    private EmailGlobalConfigAccessor configurationAccessor;

    @BeforeEach
    public void init() {
        configurationAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
    }

    @Test
    void testConfigValidTest() throws AlertException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailChannelMessagingService emailChannelMessagingService = Mockito.mock(EmailChannelMessagingService.class);
        Mockito.when(emailChannelMessagingService.sendMessage(Mockito.any(), Mockito.any())).thenReturn(new MessageResult("PASS"));

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(
            authorizationManager,
            validator,
            emailChannelMessagingService,
            javamailPropertiesFactory,
            configurationAccessor
        );

        ConfigurationTestResult testResult = emailGlobalTestAction
            .testConfigModelContent("noreply@synopsys.com", new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host"));
        assertTrue(testResult.isSuccess(), "Expected the message result to not have errors");
    }

    @Test
    void testConfigMissingDestinationTest() {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailAddressGatherer emailAddressGatherer = Mockito.mock(EmailAddressGatherer.class);
        Mockito.when(emailAddressGatherer.gatherEmailAddresses(Mockito.any(), Mockito.any())).thenReturn(Set.of());

        JobEmailAddressValidator emailAddressValidator = Mockito.mock(JobEmailAddressValidator.class);
        Mockito.when(emailAddressValidator.validate(Mockito.any(), Mockito.anyCollection())).thenReturn(new ValidatedEmailAddresses(Set.of(), Set.of()));

        MockAlertProperties testAlertProperties = new MockAlertProperties();
        MessageContentGroupCsvCreator messageContentGroupCsvCreator = new MessageContentGroupCsvCreator();
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(testAlertProperties, messageContentGroupCsvCreator, gson);

        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        EmailMessagingService emailMessagingService = new EmailMessagingService(freemarkerTemplatingService);

        EmailChannelMessagingService emailChannelMessagingService = new EmailChannelMessagingService(testAlertProperties, emailMessagingService, emailAttachmentFileCreator);

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();

        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(
            authorizationManager,
            validator,
            emailChannelMessagingService,
            javamailPropertiesFactory,
            configurationAccessor
        );

        ConfigurationTestResult testResult = emailGlobalTestAction
            .testConfigModelContent("", new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host"));
        assertFalse(testResult.isSuccess());
    }

    @Test
    void testConfigInvalidDestinationTest() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, null, null, null);

        ConfigurationTestResult testResult = emailGlobalTestAction
            .testConfigModelContent("not a valid email address", new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host"));
        assertFalse(testResult.isSuccess());
    }

    @Test
    void testSmtpPasswordMissingTest() throws AlertException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailChannelMessagingService emailChannelMessagingService = Mockito.mock(EmailChannelMessagingService.class);
        Mockito.when(emailChannelMessagingService.sendMessage(Mockito.any(), Mockito.any())).thenReturn(new MessageResult("PASS"));

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(
            authorizationManager,
            validator,
            emailChannelMessagingService,
            javamailPropertiesFactory,
            configurationAccessor
        );

        EmailGlobalConfigModel emailGlobalConfigModel = new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host");
        emailGlobalConfigModel.setIsSmtpPasswordSet(true);
        EmailGlobalConfigModel configModelWithPassword = new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host");
        configModelWithPassword.setIsSmtpPasswordSet(true);
        configModelWithPassword.setSmtpPassword("password");
        Mockito.when(configurationAccessor.getConfiguration()).thenReturn(Optional.of(configModelWithPassword));

        ConfigurationTestResult testResult = emailGlobalTestAction.testConfigModelContent("noreply@synopsys.com", emailGlobalConfigModel);
        assertTrue(testResult.isSuccess(), "Expected the message result to not have errors");
    }

    @Test
    @Disabled("Requires an SMTP server to test with")
    @Tags(value = {
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    })
    void testSmtpPasswordMissingTestIT() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        TestProperties testProperties = new TestProperties();
        String emailAddress = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        assumeTrue(testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD).isPresent());

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailChannelMessagingService validEmailChannelMessagingService = createValidEmailChannelMessagingService(emailAddress);
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(
            authorizationManager,
            validator,
            validEmailChannelMessagingService,
            javamailPropertiesFactory,
            configurationAccessor
        );

        EmailGlobalConfigModel globalConfigModelWithoutPassword = createEmailGlobalConfigModelObfuscated(testProperties);
        EmailGlobalConfigModel globalConfigModelWithPassword = createValidEmailGlobalConfigModel(testProperties);
        Mockito.when(configurationAccessor.getConfiguration()).thenReturn(Optional.of(globalConfigModelWithPassword));

        ConfigurationTestResult testResult = emailGlobalTestAction.testConfigModelContent(emailAddress, globalConfigModelWithoutPassword);
        assertTrue(testResult.isSuccess(), "Expected the message result to not have errors");
    }

    @Test
    @Tags(value = {
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    })
    void testConfigITTest() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        TestProperties testProperties = new TestProperties();
        String emailAddress = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailChannelMessagingService validEmailChannelMessagingService = createValidEmailChannelMessagingService(emailAddress);
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(
            authorizationManager,
            validator,
            validEmailChannelMessagingService,
            javamailPropertiesFactory,
            configurationAccessor
        );

        EmailGlobalConfigModel globalConfigModel = createValidEmailGlobalConfigModel(testProperties);

        ConfigurationTestResult testResult = emailGlobalTestAction.testConfigModelContent(emailAddress, globalConfigModel);
        assertTrue(testResult.isSuccess(), "Expected the message result to not have errors");
    }

    @Test
    void testPermissionForbiddenTest() throws AlertException {
        AuthorizationManager authorizationManager = createAuthorizationManager(0);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailChannelMessagingService emailChannelMessagingService = Mockito.mock(EmailChannelMessagingService.class);
        Mockito.when(emailChannelMessagingService.sendMessage(Mockito.any(), Mockito.any())).thenReturn(new MessageResult("PASS"));

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(
            authorizationManager,
            validator,
            emailChannelMessagingService,
            javamailPropertiesFactory,
            configurationAccessor
        );

        ActionResponse<ValidationResponseModel> response = emailGlobalTestAction
            .testWithPermissionCheck("noreply@synopsys.com", new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host"));
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    void testPermissionConfigValidTest() throws AlertException {
        TestProperties testProperties = new TestProperties();
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailChannelMessagingService emailChannelMessagingService = Mockito.mock(EmailChannelMessagingService.class);
        Mockito.when(emailChannelMessagingService.sendMessage(Mockito.any(), Mockito.any())).thenReturn(new MessageResult("PASS"));

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(
            authorizationManager,
            validator,
            emailChannelMessagingService,
            javamailPropertiesFactory,
            configurationAccessor
        );

        EmailGlobalConfigModel globalConfigModel = createValidEmailGlobalConfigModel(testProperties);

        ActionResponse<ValidationResponseModel> response = emailGlobalTestAction.testWithPermissionCheck("noreply@synopsys.com", globalConfigModel);
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertFalse(response.getContent().get().hasErrors(), "Expected the message result to not have errors");
    }

    @Test
    void testPermissionConfigMissingDestinationTest() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailAddressGatherer emailAddressGatherer = Mockito.mock(EmailAddressGatherer.class);
        Mockito.when(emailAddressGatherer.gatherEmailAddresses(Mockito.any(), Mockito.any())).thenReturn(Set.of());

        JobEmailAddressValidator emailAddressValidator = Mockito.mock(JobEmailAddressValidator.class);
        Mockito.when(emailAddressValidator.validate(Mockito.any(), Mockito.anyCollection())).thenReturn(new ValidatedEmailAddresses(Set.of(), Set.of()));

        MockAlertProperties testAlertProperties = new MockAlertProperties();
        MessageContentGroupCsvCreator messageContentGroupCsvCreator = new MessageContentGroupCsvCreator();
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(testAlertProperties, messageContentGroupCsvCreator, gson);

        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        EmailMessagingService emailMessagingService = new EmailMessagingService(freemarkerTemplatingService);

        EmailChannelMessagingService emailChannelMessagingService = new EmailChannelMessagingService(testAlertProperties, emailMessagingService, emailAttachmentFileCreator);

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();

        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(
            authorizationManager,
            validator,
            emailChannelMessagingService,
            javamailPropertiesFactory,
            configurationAccessor
        );

        ActionResponse<ValidationResponseModel> response = emailGlobalTestAction
            .testWithPermissionCheck("", new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host"));
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(response.hasContent());
        assertTrue(response.getContent().get().hasErrors(), "Expected the message result to not have errors");

    }

    @Test
    void testPermissionConfigInvalidDestinationTest() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(authorizationManager, validator, null, null, configurationAccessor);

        ActionResponse<ValidationResponseModel> response = emailGlobalTestAction
            .testWithPermissionCheck("not a valid email address", new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host"));
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(response.hasContent());
        assertTrue(response.getContent().get().hasErrors(), "Expected the message result to not have errors");
    }

    @Test
    @Tags(value = {
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    })
    void testPermissionConfigITTest() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        TestProperties testProperties = new TestProperties();
        String emailAddress = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailChannelMessagingService validEmailChannelMessagingService = createValidEmailChannelMessagingService(emailAddress);
        EmailGlobalTestAction emailGlobalTestAction = new EmailGlobalTestAction(
            authorizationManager,
            validator,
            validEmailChannelMessagingService,
            javamailPropertiesFactory,
            configurationAccessor
        );

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

        Gson gson = BlackDuckServicesFactory.createDefaultGson();
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
        String smtpFrom = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        String smtpHost = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        EmailGlobalConfigModel emailGlobalConfigModel = new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, smtpFrom, smtpHost);
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT).map(Integer::valueOf).ifPresent(emailGlobalConfigModel::setSmtpPort);

        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH).map(Boolean::valueOf).ifPresent(emailGlobalConfigModel::setSmtpAuth);
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER).ifPresent(emailGlobalConfigModel::setSmtpUsername);
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD).ifPresent(emailGlobalConfigModel::setSmtpPassword);

        Map<String, String> additionalPropertiesMap = new HashMap<>();
        testProperties.getOptionalProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO)
            .ifPresent(prop -> additionalPropertiesMap.put(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), prop));

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
