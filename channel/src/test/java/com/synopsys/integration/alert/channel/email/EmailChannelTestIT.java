package com.synopsys.integration.alert.channel.email;

import static com.synopsys.integration.alert.test.common.FieldModelUtils.addConfigurationFieldToMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.AbstractChannelTest;
import com.synopsys.integration.alert.channel.ChannelITTestAssertions;
import com.synopsys.integration.alert.channel.email.distribution.EmailAddressGatherer;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessageConverter;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessageFormatter;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessageSender;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelV2;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFormat;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.email.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.TestAlertProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;

public class EmailChannelTestIT extends AbstractChannelTest {
    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendEmailTest() {
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        EmailChannelKey emailChannelKey = ChannelKeys.EMAIL;
        EmailAddressGatherer emailAddressGatherer = new EmailAddressGatherer(null, null);
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(testAlertProperties, new MessageContentGroupCsvCreator(), gson);
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();

        ConfigurationModel emailGlobalConfig = createEmailGlobalConfig();
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.eq(emailChannelKey), Mockito.eq(ConfigContextEnum.GLOBAL))).thenReturn(List.of(emailGlobalConfig));

        EmailChannelMessageConverter emailChannelMessageConverter = new EmailChannelMessageConverter(new EmailChannelMessageFormatter());
        EmailChannelMessageSender emailChannelMessageSender = new EmailChannelMessageSender(emailChannelKey, testAlertProperties, emailAddressGatherer, emailAttachmentFileCreator, freemarkerTemplatingService, configurationAccessor);

        EmailChannelV2 emailChannel = new EmailChannelV2(emailChannelMessageConverter, emailChannelMessageSender);

        List<String> emailAddresses = List.of(properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT));
        EmailJobDetailsModel emailJobDetails = new EmailJobDetailsModel(null, EmailChannelTestIT.class.getSimpleName(), false, true, EmailAttachmentFormat.NONE.name(), emailAddresses);

        ChannelITTestAssertions.assertSendSimpleMessageSuccess(emailChannel, emailJobDetails);
    }

    @Test
    public void sendEmailNullGlobalTest() {
        EmailChannelKey emailChannelKey = ChannelKeys.EMAIL;

        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.eq(emailChannelKey), Mockito.eq(ConfigContextEnum.GLOBAL))).thenReturn(List.of());

        EmailChannelMessageConverter emailChannelMessageConverter = new EmailChannelMessageConverter(new EmailChannelMessageFormatter());
        EmailChannelMessageSender emailChannelMessageSender = new EmailChannelMessageSender(emailChannelKey, null, null, null, null, configurationAccessor);

        EmailChannelV2 emailChannel = new EmailChannelV2(emailChannelMessageConverter, emailChannelMessageSender);

        List<String> emailAddresses = List.of(properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT));
        EmailJobDetailsModel emailJobDetails = new EmailJobDetailsModel(null, EmailChannelTestIT.class.getSimpleName(), false, true, EmailAttachmentFormat.NONE.name(), emailAddresses);

        ChannelITTestAssertions.assertSendSimpleMessageException(emailChannel, emailJobDetails, "ERROR: Missing Email global config.");
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

