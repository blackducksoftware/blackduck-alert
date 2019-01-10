package com.synopsys.integration.alert.channel.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckEmailHandler;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class EmailChannelTestIT extends ChannelTest {

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendEmailTest() throws Exception {
        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);

        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(new Gson(), testAlertProperties, null);
        globalProperties.setBlackDuckUrl(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));

        final BlackDuckEmailHandler blackDuckEmailHandler = new BlackDuckEmailHandler(Mockito.mock(BlackDuckProjectRepositoryAccessor.class), Mockito.mock(UserProjectRelationRepositoryAccessor.class), Mockito.mock(
            BlackDuckUserRepositoryAccessor.class));
        final BlackDuckProvider blackDuckProvider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(blackDuckProvider.getEmailHandler()).thenReturn(blackDuckEmailHandler);

        final BlackDuckDescriptor blackDuckDescriptor = Mockito.mock(BlackDuckDescriptor.class);
        Mockito.when(blackDuckDescriptor.getProvider()).thenReturn(blackDuckProvider);

        final DescriptorMap descriptorMap = Mockito.mock(DescriptorMap.class);
        Mockito.when(descriptorMap.getProviderDescriptor(Mockito.anyString())).thenReturn(blackDuckDescriptor);

        final EmailAddressHandler emailAddressHandler = new EmailAddressHandler(descriptorMap);

        final EmailChannel emailChannel = new EmailChannel(gson, testAlertProperties, globalProperties, auditUtility, emailAddressHandler);
        final AggregateMessageContent content = createMessageContent(getClass().getSimpleName());
        final Set<String> emailAddresses = Stream.of(properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT)).collect(Collectors.toSet());
        final String subjectLine = "Integration test subject line";

        final Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addToMap(fieldModels, EmailDescriptor.KEY_EMAIL_ADDRESSES, emailAddresses);
        addToMap(fieldModels, EmailDescriptor.KEY_SUBJECT_LINE, subjectLine);

        addToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        addToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        addToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        addToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));
        addToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        addToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        addToMap(fieldModels, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);

        final DistributionEvent event = new DistributionEvent("1L", EmailChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), content, fieldAccessor);

        emailChannel.sendAuditedMessage(event);
    }

    @Test
    public void sendEmailNullGlobalTest() throws Exception {
        try {
            final EmailChannel emailChannel = new EmailChannel(gson, null, null, null, null);
            final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
            final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());

            final Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
            final FieldAccessor fieldAccessor = new FieldAccessor(fieldMap);
            final DistributionEvent event = new DistributionEvent("1L", EmailChannel.COMPONENT_NAME, RestConstants.formatDate(new Date()), BlackDuckProvider.COMPONENT_NAME, "FORMAT", content, fieldAccessor);
            emailChannel.sendMessage(event);
            fail();
        } catch (final IntegrationException e) {
            assertEquals("ERROR: Missing global config.", e.getMessage());
        }
    }
}

