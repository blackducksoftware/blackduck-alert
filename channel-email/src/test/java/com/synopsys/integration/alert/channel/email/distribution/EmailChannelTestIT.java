package com.synopsys.integration.alert.channel.email.distribution;

import static com.synopsys.integration.alert.test.common.FieldModelUtils.addConfigurationFieldToMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.EmailITTestAssertions;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFormat;
import com.synopsys.integration.alert.channel.email.attachment.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.channel.email.distribution.address.EmailAddressGatherer;
import com.synopsys.integration.alert.channel.email.distribution.address.JobEmailAddressValidator;
import com.synopsys.integration.alert.channel.email.distribution.address.ValidatedEmailAddresses;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;

public class EmailChannelTestIT {
    protected Gson gson;
    protected TestProperties properties;
    protected ContentConverter contentConverter;

    @BeforeEach
    public void init() {
        gson = new Gson();
        properties = new TestProperties();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
    }

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendEmailTest() {
        MockAlertProperties testAlertProperties = new MockAlertProperties();
        String testEmailRecipient = properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        EmailChannelKey emailChannelKey = ChannelKeys.EMAIL;
        EmailAddressGatherer emailAddressGatherer = new EmailAddressGatherer(null, null);
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(testAlertProperties, new MessageContentGroupCsvCreator(), gson);
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();

        ConfigurationModel emailGlobalConfig = createEmailGlobalConfig();
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.eq(emailChannelKey), Mockito.eq(ConfigContextEnum.GLOBAL))).thenReturn(List.of(emailGlobalConfig));

        JobEmailAddressValidator emailAddressValidator = Mockito.mock(JobEmailAddressValidator.class);
        Mockito.when(emailAddressValidator.validate(Mockito.any(), Mockito.anyCollection())).thenReturn(new ValidatedEmailAddresses(Set.of(testEmailRecipient), Set.of()));

        EmailChannelMessageConverter emailChannelMessageConverter = new EmailChannelMessageConverter(new EmailChannelMessageFormatter());
        EmailChannelMessageSender emailChannelMessageSender = new EmailChannelMessageSender(emailChannelKey, testAlertProperties, emailAddressValidator, emailAddressGatherer, emailAttachmentFileCreator, freemarkerTemplatingService,
            configurationAccessor);

        EmailChannel emailChannel = new EmailChannel(emailChannelMessageConverter, emailChannelMessageSender);

        List<String> emailAddresses = List.of(testEmailRecipient);
        EmailJobDetailsModel emailJobDetails = new EmailJobDetailsModel(null, EmailChannelTestIT.class.getSimpleName(), false, true, EmailAttachmentFormat.NONE.name(), emailAddresses);

        EmailITTestAssertions.assertSendSimpleMessageSuccess(emailChannel, emailJobDetails);
    }

    @Test
    public void sendEmailNullGlobalTest() {
        EmailChannelKey emailChannelKey = ChannelKeys.EMAIL;
        String testEmailRecipient = properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);

        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.eq(emailChannelKey), Mockito.eq(ConfigContextEnum.GLOBAL))).thenReturn(List.of());

        JobEmailAddressValidator emailAddressValidator = Mockito.mock(JobEmailAddressValidator.class);
        Mockito.when(emailAddressValidator.validate(Mockito.any(), Mockito.anyCollection())).thenReturn(new ValidatedEmailAddresses(Set.of(testEmailRecipient), Set.of()));

        EmailChannelMessageConverter emailChannelMessageConverter = new EmailChannelMessageConverter(new EmailChannelMessageFormatter());
        EmailChannelMessageSender emailChannelMessageSender = new EmailChannelMessageSender(emailChannelKey, null, emailAddressValidator, null, null, null, configurationAccessor);

        EmailChannel emailChannel = new EmailChannel(emailChannelMessageConverter, emailChannelMessageSender);

        List<String> emailAddresses = List.of(testEmailRecipient);
        EmailJobDetailsModel emailJobDetails = new EmailJobDetailsModel(null, EmailChannelTestIT.class.getSimpleName(), false, true, EmailAttachmentFormat.NONE.name(), emailAddresses);

        EmailITTestAssertions.assertSendSimpleMessageException(emailChannel, emailJobDetails, "ERROR: Missing Email global config.");
    }

    private ConfigurationModel createEmailGlobalConfig() {
        Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));
        return new ConfigurationModel(-1L, -1L, null, null, ConfigContextEnum.DISTRIBUTION, fieldModels);
    }

}

