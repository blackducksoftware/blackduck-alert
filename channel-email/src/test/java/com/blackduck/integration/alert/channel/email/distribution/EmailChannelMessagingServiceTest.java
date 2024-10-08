package com.blackduck.integration.alert.channel.email.distribution;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.alert.channel.email.attachment.EmailAttachmentFileCreator;
import com.blackduck.integration.alert.channel.email.attachment.EmailAttachmentFormat;
import com.blackduck.integration.alert.channel.email.attachment.MessageContentGroupCsvCreator;
import com.blackduck.integration.alert.service.email.EmailMessagingService;
import com.blackduck.integration.alert.service.email.EmailTarget;
import com.blackduck.integration.alert.service.email.SmtpConfig;
import com.blackduck.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.blackduck.integration.alert.service.email.template.FreemarkerTemplatingService;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;
import com.synopsys.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

@ExtendWith(SpringExtension.class)
class EmailChannelMessagingServiceTest {
    @Mock
    ProjectMessage projectMessage;
    @Mock
    EmailMessagingService emailMessagingService;

    private static final String EXPECTED_SUBJECT_LINE = "Subject line";
    private static final String EXPECTED_CONTENT = "content";
    private static final String EXPECTED_PROVIDER_NAME = "providerName";
    private static final String EXPECTED_PROVIDER_URL = "www.example.com/provider";
    private static final String EXPECTED_EMAIL_ADDRESS_A = "noreply@synopsys.com";
    private static final String EXPECTED_EMAIL_ADDRESS_B = "noreply@example.synopsys.com";

    private static MockAlertProperties alertProperties;
    private static EmailAttachmentFileCreator emailAttachmentFileCreator;
    private static EmailChannelMessagingService emailChannelMessagingService;

    @BeforeEach
    void initializeEmailChannelMessagingService() {
        // For a unit test we don't want to actually send emails -- rotte SEPT 2021
        Mockito.doCallRealMethod().when(emailMessagingService).addTemplateImage(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        alertProperties = new MockAlertProperties();
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        MessageContentGroupCsvCreator messageContentGroupCsvCreator = new MessageContentGroupCsvCreator();
        emailAttachmentFileCreator = new EmailAttachmentFileCreator(alertProperties, messageContentGroupCsvCreator, gson);

        emailChannelMessagingService = new EmailChannelMessagingService(alertProperties, emailMessagingService, emailAttachmentFileCreator);
    }

    @Test
    void sendMessageReturnsExpectedTest() {
        EmailChannelMessageModel emailChannelMessageModel = EmailChannelMessageModel.simple(EXPECTED_SUBJECT_LINE, EXPECTED_CONTENT, EXPECTED_PROVIDER_NAME, EXPECTED_PROVIDER_URL);

        EmailTarget emailTarget = assertDoesNotThrow(() -> emailChannelMessagingService.createTarget(emailChannelMessageModel, EXPECTED_EMAIL_ADDRESS_A, EXPECTED_EMAIL_ADDRESS_B));
        MessageResult messageResult = assertDoesNotThrow(() -> emailChannelMessagingService.sendMessage(SmtpConfig.builder().build(), emailTarget));

        assertFalse(messageResult.hasErrors());
        assertEquals("Successfully sent 2 email(s)", messageResult.getStatusMessage());
    }

    @Test
    void attachmentFileCreatedTest() {
        File tempFile = assertDoesNotThrow(() -> Files.createTempFile("Alert-email-testing", "").toFile());
        tempFile.deleteOnExit();

        EmailAttachmentFileCreator spiedEmailAttachmentFileCreator = Mockito.spy(emailAttachmentFileCreator);
        Mockito.doReturn(Optional.of(tempFile)).when(spiedEmailAttachmentFileCreator).createAttachmentFile(EmailAttachmentFormat.CSV, projectMessage);
        emailChannelMessagingService = new EmailChannelMessagingService(alertProperties, emailMessagingService, spiedEmailAttachmentFileCreator);

        EmailChannelMessageModel emailChannelMessageModel = EmailChannelMessageModel.simple(EXPECTED_SUBJECT_LINE, EXPECTED_CONTENT, EXPECTED_PROVIDER_NAME, EXPECTED_PROVIDER_URL);
        EmailTarget emailTarget = assertDoesNotThrow(() -> emailChannelMessagingService.createTarget(emailChannelMessageModel, EXPECTED_EMAIL_ADDRESS_A, EXPECTED_EMAIL_ADDRESS_B));
        MessageResult messageResult = assertDoesNotThrow(
            () -> emailChannelMessagingService.sendMessageWithAttachedProjectMessage(
                SmtpConfig.builder().build(),
                emailTarget,
                projectMessage,
                EmailAttachmentFormat.CSV
            ));

        assertFalse(messageResult.hasErrors());
        assertEquals("Successfully sent 2 email(s)", messageResult.getStatusMessage());
    }

    @Test
    void attachmentFileNotCreatedTest() {
        EmailAttachmentFileCreator spiedEmailAttachmentFileCreator = Mockito.spy(emailAttachmentFileCreator);
        Mockito.doReturn(Optional.empty()).when(spiedEmailAttachmentFileCreator).createAttachmentFile(EmailAttachmentFormat.CSV, projectMessage);
        emailChannelMessagingService = new EmailChannelMessagingService(alertProperties, emailMessagingService, spiedEmailAttachmentFileCreator);

        EmailChannelMessageModel emailChannelMessageModel = EmailChannelMessageModel.simple(EXPECTED_SUBJECT_LINE, EXPECTED_CONTENT, EXPECTED_PROVIDER_NAME, EXPECTED_PROVIDER_URL);
        EmailTarget emailTarget = assertDoesNotThrow(() -> emailChannelMessagingService.createTarget(emailChannelMessageModel, EXPECTED_EMAIL_ADDRESS_A, EXPECTED_EMAIL_ADDRESS_B));
        MessageResult messageResult = assertDoesNotThrow(
            () -> emailChannelMessagingService.sendMessageWithAttachedProjectMessage(
                SmtpConfig.builder().build(),
                emailTarget,
                projectMessage,
                EmailAttachmentFormat.CSV
            ));

        assertFalse(messageResult.hasErrors());
        assertEquals("Successfully sent 2 email(s)", messageResult.getStatusMessage());
    }

    @Test
    void createTargetReturnsExpectedTest() {
        EmailChannelMessageModel emailChannelMessageModel = EmailChannelMessageModel.simple(EXPECTED_SUBJECT_LINE, EXPECTED_CONTENT, EXPECTED_PROVIDER_NAME, EXPECTED_PROVIDER_URL);
        EmailTarget target = assertDoesNotThrow(() -> emailChannelMessagingService.createTarget(emailChannelMessageModel, EXPECTED_EMAIL_ADDRESS_A, EXPECTED_EMAIL_ADDRESS_B));

        assertTrue(target.getEmailAddresses().containsAll(Set.of(EXPECTED_EMAIL_ADDRESS_A, EXPECTED_EMAIL_ADDRESS_B)));
        assertEquals(2, target.getEmailAddresses().size());

        assertEquals(EXPECTED_SUBJECT_LINE, target.getModel().get(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey()));
        assertEquals(EXPECTED_CONTENT, target.getModel().get(EmailPropertyKeys.EMAIL_CONTENT.getPropertyKey()));
        assertEquals(EXPECTED_PROVIDER_NAME, target.getModel().get(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_NAME.getPropertyKey()));
        assertEquals(EXPECTED_PROVIDER_URL, target.getModel().get(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_URL.getPropertyKey()));
        assertTrue(target.getModel().containsKey(EmailPropertyKeys.EMAIL_CATEGORY.getPropertyKey()));
        assertTrue(target.getModel().containsKey(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_PROJECT_NAME.getPropertyKey()));
        assertTrue(target.getModel().containsKey(EmailPropertyKeys.TEMPLATE_KEY_START_DATE.getPropertyKey()));
        assertTrue(target.getModel().containsKey(EmailPropertyKeys.TEMPLATE_KEY_END_DATE.getPropertyKey()));
        assertTrue(target.getModel().containsKey(FreemarkerTemplatingService.KEY_ALERT_SERVER_URL));

    }

}
