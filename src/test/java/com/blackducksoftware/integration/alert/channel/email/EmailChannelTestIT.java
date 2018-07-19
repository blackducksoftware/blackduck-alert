package com.blackducksoftware.integration.alert.channel.email;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.OutputLogger;
import com.blackducksoftware.integration.alert.TestGlobalProperties;
import com.blackducksoftware.integration.alert.TestPropertyKey;
import com.blackducksoftware.integration.alert.audit.repository.AuditEntryRepository;
import com.blackducksoftware.integration.alert.channel.ChannelTest;
import com.blackducksoftware.integration.alert.channel.email.EmailGroupChannel;
import com.blackducksoftware.integration.alert.channel.email.mock.MockEmailEntity;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGlobalConfigEntity;
import com.blackducksoftware.integration.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.event.ChannelEvent;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubRepository;
import com.blackducksoftware.integration.test.annotation.ExternalConnectionTest;

public class EmailChannelTestIT extends ChannelTest {

    @Test
    @Category(ExternalConnectionTest.class)
    public void sendEmailTest() throws Exception {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final GlobalHubRepository globalRepository = Mockito.mock(GlobalHubRepository.class);
        final GlobalHubConfigEntity globalConfig = new GlobalHubConfigEntity(300, properties.getProperty(TestPropertyKey.TEST_HUB_API_KEY));
        Mockito.when(globalRepository.findAll()).thenReturn(Arrays.asList(globalConfig));

        final TestGlobalProperties globalProperties = new TestGlobalProperties(alertEnvironment, globalRepository);
        globalProperties.setHubUrl(properties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));

        final String trustCert = properties.getProperty(TestPropertyKey.TEST_TRUST_HTTPS_CERT);
        if (trustCert != null) {
            globalProperties.setHubTrustCertificate(Boolean.valueOf(trustCert));
        }

        EmailGroupChannel emailChannel = new EmailGroupChannel(gson, globalProperties, auditEntryRepository, null, null, null, contentConverter);
        final Collection<ProjectData> projectData = createProjectData("Manual test project");
        final DigestModel digestModel = new DigestModel(projectData);
        final ChannelEvent event = new ChannelEvent(EmailGroupChannel.COMPONENT_NAME, contentConverter.getStringValue(digestModel), 1L);

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

        final EmailGroupChannel emailChannel = new EmailGroupChannel(gson, null, null, null, null, null, contentConverter);
        final DigestModel digestModel = new DigestModel(null);
        final ChannelEvent event = new ChannelEvent(EmailGroupChannel.COMPONENT_NAME, contentConverter.getStringValue(digestModel), 1L);
        emailChannel.sendMessage(event, null);
        assertTrue(outputLogger.isLineContainingText("No configuration found with id"));

        outputLogger.close();
    }

}
