package com.synopsys.integration.alert.channel.email;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import com.synopsys.integration.alert.OutputLogger;
import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.TestBlackDuckProperties;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.test.annotation.ExternalConnectionTest;
import com.synopsys.integration.alert.channel.email.mock.MockEmailEntity;
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.common.digest.model.DigestModel;
import com.synopsys.integration.alert.common.digest.model.ProjectData;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.rest.RestConstants;

public class EmailChannelTestIT extends ChannelTest {

    @Test
    @Category(ExternalConnectionTest.class)
    public void sendEmailTest() throws Exception {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final GlobalBlackDuckRepository globalRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        final GlobalBlackDuckConfigEntity globalConfig = new GlobalBlackDuckConfigEntity(300, properties.getProperty(TestPropertyKey.TEST_HUB_API_KEY));
        Mockito.when(globalRepository.findAll()).thenReturn(Arrays.asList(globalConfig));

        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(globalRepository, testAlertProperties);
        globalProperties.setBlackDuckUrl(properties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));

        final String trustCert = properties.getProperty(TestPropertyKey.TEST_TRUST_HTTPS_CERT);
        if (trustCert != null) {
            testAlertProperties.setAlertTrustCertificate(Boolean.valueOf(trustCert));
        }

        EmailGroupChannel emailChannel = new EmailGroupChannel(gson, testAlertProperties, globalProperties, auditEntryRepository, null, null, null, contentConverter);
        final Collection<ProjectData> projectData = createProjectData("Manual test project");
        final DigestModel digestModel = new DigestModel(projectData);
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent event = new ChannelEvent(EmailGroupChannel.COMPONENT_NAME, RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), 1L, 1L);

        final String smtpHost = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        final String smtpFrom = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        final String smtpUser = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER);
        final String smtpPassword = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD);
        final Boolean smtpEhlo = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        final Boolean smtpAuth = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        final Integer smtpPort = Integer.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        final EmailGlobalConfigEntity emailGlobalConfigEntity = new EmailGlobalConfigEntity(smtpHost, smtpUser, smtpPassword, smtpPort, null, null, null, smtpFrom, null, null, null, smtpEhlo, smtpAuth, null, null, null, null, null, null,
                null,
                null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        emailChannel = Mockito.spy(emailChannel);
        Mockito.doReturn(emailGlobalConfigEntity).when(emailChannel).getGlobalConfigEntity();

        final MockEmailEntity mockEmailEntity = new MockEmailEntity();
        mockEmailEntity.setGroupName("IntegrationTest");
        emailChannel.sendAuditedMessage(event, mockEmailEntity.createEntity());
    }

    @Test
    public void sendEmailNullGlobalTest() throws Exception {
        final OutputLogger outputLogger = new OutputLogger();

        final EmailGroupChannel emailChannel = new EmailGroupChannel(gson, null, null, null, null, null, null, contentConverter);
        final DigestModel digestModel = new DigestModel(null);
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent event = new ChannelEvent(EmailGroupChannel.COMPONENT_NAME, RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), 1L, 1L);
        emailChannel.sendMessage(event, null);
        assertTrue(outputLogger.isLineContainingText("No configuration found with id"));

        outputLogger.close();
    }

}
