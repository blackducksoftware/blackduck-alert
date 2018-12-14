package com.synopsys.integration.alert.channel.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.OutputLogger;
import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.TestBlackDuckProperties;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.TestTags;
import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class EmailChannelTestIT extends ChannelTest {

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendEmailTest() throws Exception {
        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);
        final GlobalBlackDuckRepository globalRepository = Mockito.mock(GlobalBlackDuckRepository.class);

        final GlobalBlackDuckConfigEntity globalConfig = new GlobalBlackDuckConfigEntity(300, properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY), properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));
        Mockito.when(globalRepository.findAll()).thenReturn(Arrays.asList(globalConfig));

        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(new Gson(), globalRepository, testAlertProperties);
        globalProperties.setBlackDuckUrl(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));

        final String trustCert = properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TRUST_HTTPS_CERT);
        if (trustCert != null) {
            testAlertProperties.setAlertTrustCertificate(Boolean.valueOf(trustCert));
        }

        EmailGroupChannel emailChannel = new EmailGroupChannel(gson, testAlertProperties, globalProperties, auditUtility, null);
        final AggregateMessageContent content = createMessageContent(getClass().getSimpleName());
        final Set<String> emailAddresses = Stream.of(properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT)).collect(Collectors.toSet());
        final String subjectLine = "Integration test subject line";

        final DistributionEvent event = new DistributionEvent(RestConstants.formatDate(new Date()), "provider", "FORMAT", content, 1L, emailAddresses, subjectLine);

        final String smtpHost = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        final String smtpFrom = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        final String smtpUser = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER);
        final String smtpPassword = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD);
        final Boolean smtpEhlo = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        final Boolean smtpAuth = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        final Integer smtpPort = Integer.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        final EmailGlobalConfigEntity emailGlobalConfigEntity = new EmailGlobalConfigEntity(smtpHost, smtpUser, smtpPassword, smtpPort, null, null, null, smtpFrom, null, null, null, smtpEhlo, smtpAuth, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        emailChannel = Mockito.spy(emailChannel);
        Mockito.doReturn(emailGlobalConfigEntity).when(emailChannel).getGlobalConfigEntity();

        emailChannel.sendAuditedMessage(event);
    }

    @Test
    public void sendEmailNullGlobalTest() throws Exception {
        try (final OutputLogger outputLogger = new OutputLogger()) {
            final EmailGroupChannel emailChannel = new EmailGroupChannel(gson, null, null, null, null);
            final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
            final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());
            final DistributionEvent event = new DistributionEvent(RestConstants.formatDate(new Date()), "provider", "FORMAT", content, 1L, null, null);
            emailChannel.sendMessage(event);
            fail();
        } catch (final IntegrationException e) {
            assertEquals("ERROR: Missing global config.", e.getMessage());
        }
    }

}
