package com.synopsys.integration.alert.channel.email.distribution;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.EmailITTestAssertions;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFormat;
import com.synopsys.integration.alert.channel.email.attachment.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.channel.email.distribution.address.EmailAddressGatherer;
import com.synopsys.integration.alert.channel.email.distribution.address.JobEmailAddressValidator;
import com.synopsys.integration.alert.channel.email.distribution.address.ValidatedEmailAddresses;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.service.email.EmailMessagingService;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;

public class EmailChannelTestIT {
    protected Gson gson;
    protected TestProperties testProperties;

    @BeforeEach
    public void init() {
        gson = new Gson();
        testProperties = new TestProperties();
    }

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendEmailTest() {
        MockAlertProperties testAlertProperties = new MockAlertProperties();
        String testEmailRecipient = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        EmailAddressGatherer emailAddressGatherer = new EmailAddressGatherer(null, null);
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(testAlertProperties, new MessageContentGroupCsvCreator(), gson);
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        EmailMessagingService emailMessagingService = new EmailMessagingService(freemarkerTemplatingService);

        JobEmailAddressValidator emailAddressValidator = Mockito.mock(JobEmailAddressValidator.class);
        Mockito.when(emailAddressValidator.validate(Mockito.any(), Mockito.anyCollection())).thenReturn(new ValidatedEmailAddresses(Set.of(testEmailRecipient), Set.of()));

        EmailChannelMessageConverter emailChannelMessageConverter = new EmailChannelMessageConverter(new EmailChannelMessageFormatter());
        EmailChannelMessageSender emailChannelMessageSender = new EmailChannelMessageSender(testAlertProperties, emailAddressValidator, emailAddressGatherer, emailAttachmentFileCreator, emailMessagingService);

        EmailChannel emailChannel = new EmailChannel(emailChannelMessageConverter, emailChannelMessageSender);

        List<String> emailAddresses = List.of(testEmailRecipient);
        EmailJobDetailsModel emailJobDetails = new EmailJobDetailsModel(
            null,
            createJavamailProperties(),
            testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM),
            testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST),
            Integer.parseInt(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT)),
            Boolean.parseBoolean(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH)),
            testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER),
            testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD),
            EmailChannelTestIT.class.getSimpleName(),
            false,
            true,
            EmailAttachmentFormat.NONE.name(),
            emailAddresses
        );

        EmailITTestAssertions.assertSendSimpleMessageSuccess(emailChannel, emailJobDetails);
    }

    private Properties createJavamailProperties() {
        Properties properties = new Properties();
        properties.setProperty(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        properties.setProperty(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        properties.setProperty(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        properties.setProperty(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        properties.setProperty(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        properties.setProperty(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        return properties;
    }

}

