package com.synopsys.integration.alert.channel.email.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.attachment.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.service.email.EmailMessagingService;
import com.synopsys.integration.alert.service.email.EmailTarget;
import com.synopsys.integration.alert.service.email.SmtpConfig;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

public class EmailChannelMessagingServiceTest {
    public static final String EXPECTED_SUBJECT_LINE = "Subject line";
    public static final String EXPECTED_CONTENT = "content";
    public static final String EXPECTED_PROVIDER_NAME = "providerName";
    public static final String EXPECTED_PROVIDER_URL = "www.example.com/provider";
    public static final String EXPECTED_EMAIL_ADDRESS_A = "noreply@synopsys.com";
    public static final String EXPECTED_EMAIL_ADDRESS_B = "noreply@example.synopsys.com";

    private EmailChannelMessagingService emailChannelMessagingService;

    @BeforeEach
    public void initializeEmailChannelMessagingService() {
        MockAlertProperties alertProperties = new MockAlertProperties();

        // For a unit test we don't want to actually send emails -- rotte SEPT 2021
        EmailMessagingService emailMessagingService = Mockito.mock(EmailMessagingService.class);
        Mockito.doCallRealMethod().when(emailMessagingService).addTemplateImage(Mockito.any(),Mockito.any(), Mockito.any(),Mockito.any());
        //

        Gson gson = new Gson();
        MessageContentGroupCsvCreator messageContentGroupCsvCreator = new MessageContentGroupCsvCreator();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(alertProperties, messageContentGroupCsvCreator, gson);

        emailChannelMessagingService = new EmailChannelMessagingService(alertProperties, emailMessagingService, emailAttachmentFileCreator);
    }

    @Test
    public void testSendMessage() {
        EmailChannelMessageModel emailChannelMessageModel = EmailChannelMessageModel.simple(EXPECTED_SUBJECT_LINE, EXPECTED_CONTENT, EXPECTED_PROVIDER_NAME, EXPECTED_PROVIDER_URL);

        try {
            EmailTarget emailTarget = emailChannelMessagingService.createTarget(emailChannelMessageModel, EXPECTED_EMAIL_ADDRESS_A, EXPECTED_EMAIL_ADDRESS_B);
            MessageResult messageResult = emailChannelMessagingService.sendMessage(SmtpConfig.builder().build(), emailTarget);

            assertFalse(messageResult.hasErrors());
            assertEquals("Successfully sent 2 email(s)", messageResult.getStatusMessage());
        } catch (Exception e) {
            fail("Unexpected exception occurred when sending emails", e);
        }
    }

    @Test
    public void testCreateTarget() {
        EmailChannelMessageModel emailChannelMessageModel = EmailChannelMessageModel.simple(EXPECTED_SUBJECT_LINE, EXPECTED_CONTENT, EXPECTED_PROVIDER_NAME, EXPECTED_PROVIDER_URL);

        try {
            EmailTarget target = emailChannelMessagingService.createTarget(emailChannelMessageModel, EXPECTED_EMAIL_ADDRESS_A, EXPECTED_EMAIL_ADDRESS_B);

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
        } catch (Exception e) {
            fail("Unexpected exception occurred when creating the EmailTarget", e);
        }
    }

}
