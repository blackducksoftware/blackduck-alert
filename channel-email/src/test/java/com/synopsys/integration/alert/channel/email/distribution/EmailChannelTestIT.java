package com.synopsys.integration.alert.channel.email.distribution;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.channel.email.EmailITTestAssertions;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFormat;
import com.synopsys.integration.alert.channel.email.attachment.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.email.distribution.address.EmailAddressGatherer;
import com.synopsys.integration.alert.channel.email.distribution.address.JobEmailAddressValidator;
import com.synopsys.integration.alert.channel.email.distribution.address.ValidatedEmailAddresses;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.service.email.EmailMessagingService;
import com.synopsys.integration.alert.service.email.JavamailPropertiesFactory;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.service.email.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;

class EmailChannelTestIT {
    protected Gson gson;
    protected TestProperties testProperties;
    private EventManager eventManager;
    private ExecutingJobManager executingJobManager;

    @BeforeEach
    public void init() {
        gson = new Gson();
        testProperties = new TestProperties();
        eventManager = Mockito.mock(EventManager.class);
        executingJobManager = Mockito.mock(ExecutingJobManager.class);
    }

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    void sendEmailTest() {
        MockAlertProperties testAlertProperties = new MockAlertProperties();
        String testEmailRecipient = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(testAlertProperties, new MessageContentGroupCsvCreator(), gson);
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        EmailMessagingService emailMessagingService = new EmailMessagingService(freemarkerTemplatingService);
        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailChannelMessagingService emailChannelMessagingService = new EmailChannelMessagingService(testAlertProperties, emailMessagingService, emailAttachmentFileCreator);

        EmailGlobalConfigModel emailGlobalConfig = createEmailGlobalConfig();
        EmailGlobalConfigAccessor emailConfigurationAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(emailConfigurationAccessor.getConfiguration()).thenReturn(Optional.of(emailGlobalConfig));

        JobEmailAddressValidator emailAddressValidator = Mockito.mock(JobEmailAddressValidator.class);
        Mockito.when(emailAddressValidator.validate(Mockito.any(), Mockito.anyCollection())).thenReturn(new ValidatedEmailAddresses(Set.of(testEmailRecipient), Set.of()));

        EmailAddressGatherer emailAddressGatherer = new EmailAddressGatherer(null, null);
        EmailChannelMessageConverter emailChannelMessageConverter = new EmailChannelMessageConverter(new EmailChannelMessageFormatter());

        EmailChannelMessageSender emailChannelMessageSender = new EmailChannelMessageSender(
            emailConfigurationAccessor,
            emailAddressGatherer,
            emailChannelMessagingService,
            emailAddressValidator,
            javamailPropertiesFactory
        );
        EmailChannel emailChannel = new EmailChannel(emailChannelMessageConverter, emailChannelMessageSender, eventManager, executingJobManager);

        List<String> emailAddresses = List.of(testEmailRecipient);
        EmailJobDetailsModel emailJobDetails = new EmailJobDetailsModel(
            null,
            EmailChannelTestIT.class.getSimpleName(),
            false,
            true,
            EmailAttachmentFormat.NONE.name(),
            emailAddresses
        );

        EmailITTestAssertions.assertSendSimpleMessageSuccess(emailChannel, emailJobDetails);
    }

    @Test
    public void sendEmailNullGlobalTest() {
        String testEmailRecipient = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        EmailGlobalConfigAccessor emailConfigurationAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(emailConfigurationAccessor.getConfiguration()).thenReturn(Optional.empty());

        JobEmailAddressValidator emailAddressValidator = Mockito.mock(JobEmailAddressValidator.class);
        Mockito.when(emailAddressValidator.validate(Mockito.any(), Mockito.anyCollection())).thenReturn(new ValidatedEmailAddresses(Set.of(testEmailRecipient), Set.of()));

        EmailAddressGatherer emailAddressGatherer = new EmailAddressGatherer(null, null);
        EmailChannelMessageConverter emailChannelMessageConverter = new EmailChannelMessageConverter(new EmailChannelMessageFormatter());
        EmailChannelMessageSender emailChannelMessageSender = new EmailChannelMessageSender(emailConfigurationAccessor, emailAddressGatherer, null, emailAddressValidator, null);

        EmailChannel emailChannel = new EmailChannel(emailChannelMessageConverter, emailChannelMessageSender, eventManager, executingJobManager);

        List<String> emailAddresses = List.of(testEmailRecipient);
        EmailJobDetailsModel emailJobDetails = new EmailJobDetailsModel(
            null,
            EmailChannelTestIT.class.getSimpleName(),
            false,
            true,
            EmailAttachmentFormat.NONE.name(),
            emailAddresses
        );

        EmailITTestAssertions.assertSendSimpleMessageException(emailChannel, emailJobDetails, "ERROR: Missing Email global config.");
    }

    private EmailGlobalConfigModel createEmailGlobalConfig() {
        String smtpFrom = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        String smtpHost = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        EmailGlobalConfigModel emailGlobalConfigModel = new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, smtpFrom, smtpHost);
        emailGlobalConfigModel.setSmtpUsername(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        emailGlobalConfigModel.setSmtpPassword(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));

        emailGlobalConfigModel.setSmtpAuth(Boolean.parseBoolean(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH)));
        String port = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT);
        if (StringUtils.isNotBlank(port)) {
            emailGlobalConfigModel.setSmtpPort(Integer.parseInt(port));
        }
        Map<String, String> properties = Map.of(TestPropertyKey.TEST_EMAIL_SMTP_EHLO.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        emailGlobalConfigModel.setAdditionalJavaMailProperties(properties);
        return emailGlobalConfigModel;
    }

}

