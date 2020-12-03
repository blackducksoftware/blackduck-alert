package com.synopsys.integration.alert.channel.email;

import static com.synopsys.integration.alert.test.common.FieldModelUtils.addConfigurationFieldToMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.AbstractChannelTest;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.template.EmailChannelMessageParser;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.email.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.test.common.TestAlertProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class EmailChannelTestIT extends AbstractChannelTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();
    private static final EmailChannelKey CHANNEL_KEY = new EmailChannelKey();

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendEmailTest() throws Exception {
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        EmailAddressHandler emailAddressHandler = new EmailAddressHandler(Mockito.mock(ProviderDataAccessor.class));

        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        EmailChannelMessageParser emailChannelMessageParser = new EmailChannelMessageParser();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(testAlertProperties, new MessageContentGroupCsvCreator(), gson);
        EmailChannel emailChannel = new EmailChannel(CHANNEL_KEY, gson, testAlertProperties, auditAccessor, emailAddressHandler, freemarkerTemplatingService, emailChannelMessageParser, emailAttachmentFileCreator);
        ProviderMessageContent content = createMessageContent(getClass().getSimpleName());
        Set<String> emailAddresses = Set.of(properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT));
        String subjectLine = "Integration test subject line";

        Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, EmailDescriptor.KEY_EMAIL_ADDRESSES, emailAddresses);
        addConfigurationFieldToMap(fieldModels, EmailDescriptor.KEY_SUBJECT_LINE, subjectLine);

        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        FieldUtility fieldUtility = new FieldUtility(fieldModels);
        DistributionEvent event = new DistributionEvent(
            "1L", CHANNEL_KEY.getUniversalKey(), RestConstants.formatDate(new Date()), 1L, ProcessingType.DEFAULT.name(), MessageContentGroup.singleton(content), fieldUtility);
        emailChannel.sendAuditedMessage(event);
        Mockito.verify(auditAccessor).setAuditEntrySuccess(Mockito.any());
    }

    @Test
    public void sendEmailNullGlobalTest() throws Exception {
        EmailChannelMessageParser emailChannelMessageParser = new EmailChannelMessageParser();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(null, new MessageContentGroupCsvCreator(), gson);
        EmailChannel emailChannel = new EmailChannel(CHANNEL_KEY, gson, null, null, null, null, emailChannelMessageParser, emailAttachmentFileCreator);
        LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyTopic("testTopic", "topic")
                                             .applySubTopic(subTopic.getName(), subTopic.getValue())
                                             .build();
        try {
            Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
            FieldUtility fieldUtility = new FieldUtility(fieldMap);
            DistributionEvent event = new DistributionEvent(
                "1L", CHANNEL_KEY.getUniversalKey(), RestConstants.formatDate(new Date()), 1L, "FORMAT", MessageContentGroup.singleton(content), fieldUtility);
            emailChannel.sendMessage(event);
            fail();
        } catch (IntegrationException e) {
            assertEquals("ERROR: Missing global config.", e.getMessage());
        }
    }

}

