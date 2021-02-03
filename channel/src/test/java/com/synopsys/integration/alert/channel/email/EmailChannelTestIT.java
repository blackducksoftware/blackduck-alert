package com.synopsys.integration.alert.channel.email;

import static com.synopsys.integration.alert.test.common.FieldModelUtils.addConfigurationFieldToMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.AbstractChannelTest;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.template.EmailChannelMessageParser;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.email.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.TestAlertProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class EmailChannelTestIT extends AbstractChannelTest {
    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendEmailTest() throws Exception {
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        EmailAddressHandler emailAddressHandler = new EmailAddressHandler(Mockito.mock(ProviderDataAccessor.class));

        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        EmailChannelMessageParser emailChannelMessageParser = new EmailChannelMessageParser();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(testAlertProperties, new MessageContentGroupCsvCreator(), gson);
        EmailChannel emailChannel = new EmailChannel(gson, testAlertProperties, auditAccessor, emailAddressHandler, freemarkerTemplatingService, emailChannelMessageParser, emailAttachmentFileCreator);
        ProviderMessageContent content = createMessageContent(getClass().getSimpleName());
        Set<String> emailAddresses = Set.of(properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT));
        String subjectLine = "Integration test subject line";

        Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        ConfigurationModel emailGlobalConfig = new ConfigurationModel(-1L, -1L, null, null, ConfigContextEnum.DISTRIBUTION, fieldModels);
        DistributionJobModel testJobModel = createTestJobModel(subjectLine, emailAddresses);

        DistributionEvent event = new DistributionEvent(
            ChannelKeys.EMAIL.getUniversalKey(), RestConstants.formatDate(new Date()), 1L, ProcessingType.DEFAULT.name(), MessageContentGroup.singleton(content), testJobModel, emailGlobalConfig);
        emailChannel.sendAuditedMessage(event);
        Mockito.verify(auditAccessor).setAuditEntrySuccess(Mockito.any());
    }

    @Test
    public void sendEmailNullGlobalTest() throws Exception {
        EmailChannelMessageParser emailChannelMessageParser = new EmailChannelMessageParser();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(null, new MessageContentGroupCsvCreator(), gson);
        EmailChannel emailChannel = new EmailChannel(gson, null, null, null, null, emailChannelMessageParser, emailAttachmentFileCreator);
        LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyTopic("testTopic", "topic")
                                             .applySubTopic(subTopic.getLabel(), subTopic.getValue())
                                             .build();
        try {
            DistributionJobModel testJobModel = createTestJobModel("Null Global Test", List.of());
            DistributionEvent event = new DistributionEvent(ChannelKeys.EMAIL.getUniversalKey(), RestConstants.formatDate(new Date()), 1L, "FORMAT", MessageContentGroup.singleton(content), testJobModel, null);
            emailChannel.sendMessage(event);
            fail("Expected exception to be thrown for null global config");
        } catch (IntegrationException e) {
            assertEquals("ERROR: Missing Email global config.", e.getMessage());
        }
    }

    private DistributionJobModel createTestJobModel(String subjectLine, Collection<String> emailAddresses) {
        EmailJobDetailsModel jobDetailsModel = new EmailJobDetailsModel(
            null,
            subjectLine,
            false,
            true,
            null,
            List.copyOf(emailAddresses)
        );
        return DistributionJobModel.builder()
                   .distributionJobDetails(jobDetailsModel)
                   .build();
    }

}

