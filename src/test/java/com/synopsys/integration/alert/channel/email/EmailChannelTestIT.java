package com.synopsys.integration.alert.channel.email;

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.util.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.message.model2.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.alert.database.api.DefaultProviderDataAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class EmailChannelTestIT extends ChannelTest {
    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendEmailTest() throws Exception {
        final DefaultAuditUtility auditUtility = Mockito.mock(DefaultAuditUtility.class);

        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testBlackDuckProperties = new TestBlackDuckProperties(new Gson(), testAlertProperties, null, null);
        testBlackDuckProperties.setBlackDuckUrl(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));

        final PolarisProperties testPolarisProperties = Mockito.mock(PolarisProperties.class);
        final String polarisUrl = properties.getProperty(TestPropertyKey.TEST_POLARIS_PROVIDER_URL);
        Mockito.when(testPolarisProperties.getUrl()).thenReturn(Optional.ofNullable(polarisUrl));

        final EmailAddressHandler emailAddressHandler = new EmailAddressHandler(Mockito.mock(DefaultProviderDataAccessor.class));

        final FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(testAlertProperties);
        final EmailChannel emailChannel = new EmailChannel(gson, testAlertProperties, testBlackDuckProperties, testPolarisProperties, auditUtility, emailAddressHandler, freemarkerTemplatingService);
        final ProviderMessageContent content = createMessageContent(getClass().getSimpleName());
        final Set<String> emailAddresses = Set.of(properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT));
        final String subjectLine = "Integration test subject line";

        final Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, EmailDescriptor.KEY_EMAIL_ADDRESSES, emailAddresses);
        addConfigurationFieldToMap(fieldModels, EmailDescriptor.KEY_SUBJECT_LINE, subjectLine);

        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        addConfigurationFieldToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        final DistributionEvent event = new DistributionEvent(
            "1L", EmailChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), MessageContentGroup.singleton(content), fieldAccessor);
        emailChannel.sendAuditedMessage(event);
    }

    @Test
    public void sendEmailNullGlobalTest() {
        try {
            final EmailChannel emailChannel = new EmailChannel(gson, null, null, null, null, null, null);
            final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
            final ProviderMessageContent content = new ProviderMessageContent.Builder()
                                                       .applyProvider("testProvider")
                                                       .applyTopic("testTopic", "topic")
                                                       .applySubTopic(subTopic.getName(), subTopic.getValue())
                                                       .build();

            final Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
            final FieldAccessor fieldAccessor = new FieldAccessor(fieldMap);
            final DistributionEvent event = new DistributionEvent(
                "1L", EmailChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, "FORMAT", MessageContentGroup.singleton(content), fieldAccessor);
            emailChannel.sendMessage(event);
            fail();
        } catch (final IntegrationException e) {
            assertEquals("ERROR: Missing global config.", e.getMessage());
        }
    }

}

