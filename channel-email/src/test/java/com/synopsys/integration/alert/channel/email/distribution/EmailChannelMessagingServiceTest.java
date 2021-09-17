package com.synopsys.integration.alert.channel.email.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFormat;
import com.synopsys.integration.alert.channel.email.attachment.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.channel.email.distribution.address.EmailAddressGatherer;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
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
    public static final String INVALID_EMAIL_ADDRESS = "not an email address";

    private EmailChannelMessagingService emailChannelMessagingService;

    @BeforeEach
    public void initializeEmailChannelMessagingService() {
        MockAlertProperties alertProperties = new MockAlertProperties();

        // For a unit test we don't want to access the database or actually send emails -- rotte SEPT 2021
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        ProviderDataAccessor providerDataAccessor = Mockito.mock(ProviderDataAccessor.class);
        EmailMessagingService emailMessagingService = Mockito.mock(EmailMessagingService.class);
        Mockito.doCallRealMethod().when(emailMessagingService).addTemplateImage(Mockito.any(),Mockito.any(), Mockito.any(),Mockito.any());
        //

        EmailAddressGatherer emailAddressGatherer = new EmailAddressGatherer(jobAccessor, providerDataAccessor);

        Gson gson = new Gson();
        MessageContentGroupCsvCreator messageContentGroupCsvCreator = new MessageContentGroupCsvCreator();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(alertProperties, messageContentGroupCsvCreator, gson);

        emailChannelMessagingService = new EmailChannelMessagingService(emailAddressGatherer, alertProperties, emailMessagingService, emailAttachmentFileCreator);
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
    public void testSendMessages() {
        EmailJobDetailsModel emailJobDetailsModel = createSimpleEmailJobDetailsModel(List.of(EXPECTED_EMAIL_ADDRESS_A, EXPECTED_EMAIL_ADDRESS_B));

        EmailChannelMessageModel emailChannelMessageModel = EmailChannelMessageModel.simple(EXPECTED_SUBJECT_LINE, EXPECTED_CONTENT, EXPECTED_PROVIDER_NAME, EXPECTED_PROVIDER_URL);
        EmailChannelMessageModel emailChannelMessageModel2 = EmailChannelMessageModel.simple(EXPECTED_SUBJECT_LINE, EXPECTED_CONTENT, EXPECTED_PROVIDER_NAME, EXPECTED_PROVIDER_URL);

        try {
            MessageResult messageResult = emailChannelMessagingService.sendMessages(SmtpConfig.builder().build(), emailJobDetailsModel, List.of(emailChannelMessageModel, emailChannelMessageModel2), Set.of(INVALID_EMAIL_ADDRESS));

            assertFalse(messageResult.hasErrors());
            assertEquals("Successfully sent 4 email(s)", messageResult.getStatusMessage());
        } catch (Exception e) {
            fail("Unexpected exception occurred when sending emails", e);
        }
    }

    @Test
    public void testSendMessagesInvalidEmails() {
        EmailJobDetailsModel emailJobDetailsModel = createSimpleEmailJobDetailsModel(List.of());

        EmailChannelMessageModel emailChannelMessageModel = EmailChannelMessageModel.simple(EXPECTED_SUBJECT_LINE, EXPECTED_CONTENT, EXPECTED_PROVIDER_NAME, EXPECTED_PROVIDER_URL);
        EmailChannelMessageModel emailChannelMessageModel2 = EmailChannelMessageModel.simple(EXPECTED_SUBJECT_LINE, EXPECTED_CONTENT, EXPECTED_PROVIDER_NAME, EXPECTED_PROVIDER_URL);

        try {
            emailChannelMessagingService.sendMessages(SmtpConfig.builder().build(), emailJobDetailsModel, List.of(emailChannelMessageModel, emailChannelMessageModel2), Set.of(INVALID_EMAIL_ADDRESS));

            fail("Even though no valid email addresses were provided, the expected exception was not thrown");
        } catch (AlertException e) {
            assertEquals("No valid email addresses to send this content to. The following email addresses were invalid: " + INVALID_EMAIL_ADDRESS, e.getMessage());
        }
    }

    @Test
    public void testSendMessagesNoEmails() {
        EmailJobDetailsModel emailJobDetailsModel = createSimpleEmailJobDetailsModel(List.of());

        EmailChannelMessageModel emailChannelMessageModel = EmailChannelMessageModel.simple(EXPECTED_SUBJECT_LINE, EXPECTED_CONTENT, EXPECTED_PROVIDER_NAME, EXPECTED_PROVIDER_URL);
        EmailChannelMessageModel emailChannelMessageModel2 = EmailChannelMessageModel.simple(EXPECTED_SUBJECT_LINE, EXPECTED_CONTENT, EXPECTED_PROVIDER_NAME, EXPECTED_PROVIDER_URL);

        try {
            emailChannelMessagingService.sendMessages(SmtpConfig.builder().build(), emailJobDetailsModel, List.of(emailChannelMessageModel, emailChannelMessageModel2), Set.of());

            fail("Even though no email addresses were provided, the expected exception was not thrown");
        } catch (AlertException e) {
            assertEquals("Could not determine what email addresses to send this content to", e.getMessage());
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

    private EmailJobDetailsModel createSimpleEmailJobDetailsModel(List<String> emailAddresses) {
        return new EmailJobDetailsModel(
            new UUID(0L,0L),
            EXPECTED_SUBJECT_LINE,
            false,
            true,
            EmailAttachmentFormat.NONE.toString(),
            emailAddresses
        );
    }

}
